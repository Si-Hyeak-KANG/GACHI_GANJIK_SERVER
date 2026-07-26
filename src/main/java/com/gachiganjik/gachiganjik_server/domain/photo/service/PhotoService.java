package com.gachiganjik.gachiganjik_server.domain.photo.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.album.entity.*;
import com.gachiganjik.gachiganjik_server.domain.album.repository.AlbumMemberRepository;
import com.gachiganjik.gachiganjik_server.domain.album.repository.AlbumRepository;
import com.gachiganjik.gachiganjik_server.domain.album.service.AlbumEventPublisher;
import com.gachiganjik.gachiganjik_server.domain.comment.entity.CommentStatus;
import com.gachiganjik.gachiganjik_server.domain.comment.entity.ReactionType;
import com.gachiganjik.gachiganjik_server.domain.comment.repository.CommentRepository;
import com.gachiganjik.gachiganjik_server.domain.comment.repository.PhotoReactionRepository;
import com.gachiganjik.gachiganjik_server.domain.guest.entity.GuestInfo;
import com.gachiganjik.gachiganjik_server.domain.guest.entity.GuestStatus;
import com.gachiganjik.gachiganjik_server.domain.guest.repository.GuestInfoRepository;
import com.gachiganjik.gachiganjik_server.domain.photo.dto.PhotoDto;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.*;
import com.gachiganjik.gachiganjik_server.domain.photo.repository.MomentRepository;
import com.gachiganjik.gachiganjik_server.domain.photo.repository.PhotoRepository;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoService {

    private static final int MAX_PHOTOS_PER_UPLOAD = 10;

    private final PhotoRepository photoRepository;
    private final MomentRepository momentRepository;
    private final AlbumRepository albumRepository;
    private final AlbumMemberRepository albumMemberRepository;
    private final UserInfoRepository userInfoRepository;
    private final PhotoReactionRepository photoReactionRepository;
    private final CommentRepository commentRepository;
    private final GuestInfoRepository guestInfoRepository;
    private final AlbumEventPublisher albumEventPublisher;

    // ──────────────────────────────────────────
    // MEMBER
    // ──────────────────────────────────────────

    @Transactional
    public PhotoDto.PhotoUploadResponse uploadPhotos(Long userId, Long albumId, PhotoDto.PhotoUploadRequest request) {
        if (request.photos().size() > MAX_PHOTOS_PER_UPLOAD) {
            throw new BusinessException(ErrorCode.TOO_MANY_FILES);
        }

        Album album = findActiveAlbum(albumId);
        UserInfo uploader = findUser(userId);
        findActiveMember(album, uploader);

        LocalDate photoDate = request.photoDate() != null
                ? LocalDate.parse(request.photoDate())
                : LocalDate.now();

        String clientMomentId = request.momentId() != null
                ? request.momentId()
                : UUID.randomUUID().toString();

        Moment moment = createMoment(album, clientMomentId, photoDate, request.message());

        List<Photo> saved = request.photos().stream()
                .map(item -> photoRepository.save(Photo.builder()
                        .moment(moment)
                        .album(album)
                        .uploaderUser(uploader)
                        .imageUrl(item.imageUrl())
                        .thumbnailUrl(item.thumbnailUrl())
                        .message(item.message())
                        .photoDate(photoDate)
                        .colorCode(item.colorCode())
                        .build()))
                .toList();

        LocalDateTime uploadedAt = LocalDateTime.now();
        album.updateLastPhotoUploadedAt(uploadedAt);
        int totalPhotoCount = photoRepository.countByAlbumAndStatus(albumId, PhotoStatus.ACTIVE);
        albumEventPublisher.publishPhotoUploaded(albumId, uploader.getNickname(), totalPhotoCount, uploadedAt);

        return new PhotoDto.PhotoUploadResponse(
                saved.stream().map(p -> PhotoDto.PhotoSummary.of(p, 0, 0, false)).toList()
        );
    }

    public PhotoDto.PhotoListResponse getPhotos(Long userId, Long albumId, int page, int size) {
        Album album = findActiveAlbum(albumId);
        findActiveMember(album, findUser(userId));
        return buildPhotoListResponse(album, page, size,
                photoId -> photoReactionRepository.existsByPhotoPhotoIdAndUserInfoUserIdAndReactionType(
                        photoId, userId, ReactionType.LIKE));
    }

    public PhotoDto.PhotoDetailResponse getPhotoDetail(Long userId, Long albumId, Long photoId) {
        Album album = findActiveAlbum(albumId);
        findActiveMember(album, findUser(userId));
        Photo photo = findActivePhoto(photoId);
        validatePhotoInAlbum(photo, albumId);
        int likeCount = photoReactionRepository.countByPhotoIdAndReactionType(photo.getPhotoId(), ReactionType.LIKE);
        return PhotoDto.PhotoDetailResponse.of(photo, likeCount);
    }

    @Transactional
    public PhotoDto.PhotoMessageUpdateResponse updateMessage(Long userId, Long albumId, Long photoId,
                                                             PhotoDto.PhotoMessageUpdateRequest request) {
        Album album = findActiveAlbum(albumId);
        findActiveMember(album, findUser(userId));
        Photo photo = findActivePhoto(photoId);
        validatePhotoInAlbum(photo, albumId);

        if (photo.getUploaderUser() == null || !photo.getUploaderUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_PHOTO_OWNER);
        }

        photo.updateMessage(request.message());
        return PhotoDto.PhotoMessageUpdateResponse.of(photo);
    }

    @Transactional
    public void deletePhoto(Long userId, Long albumId, Long photoId) {
        Album album = findActiveAlbum(albumId);
        UserInfo user = findUser(userId);
        AlbumMember member = findActiveMember(album, user);
        Photo photo = findActivePhoto(photoId);
        validatePhotoInAlbum(photo, albumId);

        boolean isUploader = photo.getUploaderUser() != null
                && photo.getUploaderUser().getUserId().equals(userId);
        boolean isManagerRole = member.getRole() == AlbumRole.OWNER
                || member.getRole() == AlbumRole.ADMIN;

        if (!isUploader && !isManagerRole) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        photo.delete();
    }

    public PhotoDto.PhotoDownloadResponse getDownloadUrl(Long userId, Long albumId, Long photoId) {
        Album album = findActiveAlbum(albumId);
        findActiveMember(album, findUser(userId));
        Photo photo = findActivePhoto(photoId);
        validatePhotoInAlbum(photo, albumId);
        return PhotoDto.PhotoDownloadResponse.of(photo);
    }

    // ──────────────────────────────────────────
    // GUEST
    // ──────────────────────────────────────────

    @Transactional
    public PhotoDto.PhotoUploadResponse uploadPhotosAsGuest(Long guestId, Long albumId,
                                                            PhotoDto.PhotoUploadRequest request) {
        if (request.photos().size() > MAX_PHOTOS_PER_UPLOAD) {
            throw new BusinessException(ErrorCode.TOO_MANY_FILES);
        }

        Album album = findActiveAlbum(albumId);
        GuestInfo guest = findActiveGuest(guestId);
        validateGuestMembership(album, guest);

        LocalDate photoDate = request.photoDate() != null
                ? LocalDate.parse(request.photoDate())
                : LocalDate.now();

        String clientMomentId = request.momentId() != null
                ? request.momentId()
                : UUID.randomUUID().toString();

        Moment moment = createMoment(album, clientMomentId, photoDate, request.message());

        List<Photo> saved = request.photos().stream()
                .map(item -> photoRepository.save(Photo.builder()
                        .moment(moment)
                        .album(album)
                        .uploaderUser(null)
                        .imageUrl(item.imageUrl())
                        .thumbnailUrl(item.thumbnailUrl())
                        .message(item.message())
                        .photoDate(photoDate)
                        .colorCode(item.colorCode())
                        .build()))
                .toList();

        LocalDateTime uploadedAt = LocalDateTime.now();
        album.updateLastPhotoUploadedAt(uploadedAt);
        int totalPhotoCount = photoRepository.countByAlbumAndStatus(albumId, PhotoStatus.ACTIVE);
        albumEventPublisher.publishPhotoUploaded(albumId, guest.getNickname(), totalPhotoCount, uploadedAt);

        return new PhotoDto.PhotoUploadResponse(
                saved.stream().map(p -> PhotoDto.PhotoSummary.of(p, 0, 0, false)).toList()
        );
    }

    public PhotoDto.PhotoListResponse getPhotosAsGuest(Long guestId, Long albumId, int page, int size) {
        Album album = findActiveAlbum(albumId);
        GuestInfo guest = findActiveGuest(guestId);
        validateGuestMembership(album, guest);
        return buildPhotoListResponse(album, page, size,
                photoId -> photoReactionRepository.existsByPhotoPhotoIdAndGuestInfoGuestIdAndReactionType(
                        photoId, guestId, ReactionType.LIKE));
    }

    public PhotoDto.PhotoDetailResponse getPhotoDetailAsGuest(Long guestId, Long albumId, Long photoId) {
        Album album = findActiveAlbum(albumId);
        GuestInfo guest = findActiveGuest(guestId);
        validateGuestMembership(album, guest);
        Photo photo = findActivePhoto(photoId);
        validatePhotoInAlbum(photo, albumId);
        int likeCount = photoReactionRepository.countByPhotoIdAndReactionType(photo.getPhotoId(), ReactionType.LIKE);
        return PhotoDto.PhotoDetailResponse.of(photo, likeCount);
    }

    @Transactional
    public void deletePhotoAsGuest(Long guestId, Long albumId, Long photoId) {
        Album album = findActiveAlbum(albumId);
        GuestInfo guest = findActiveGuest(guestId);
        validateGuestMembership(album, guest);
        Photo photo = findActivePhoto(photoId);
        validatePhotoInAlbum(photo, albumId);

        if (photo.getUploaderUser() != null) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        photo.delete();
    }

    public PhotoDto.PhotoDownloadResponse getDownloadUrlAsGuest(Long guestId, Long albumId, Long photoId) {
        Album album = findActiveAlbum(albumId);
        GuestInfo guest = findActiveGuest(guestId);
        validateGuestMembership(album, guest);
        Photo photo = findActivePhoto(photoId);
        validatePhotoInAlbum(photo, albumId);
        return PhotoDto.PhotoDownloadResponse.of(photo);
    }

    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    @FunctionalInterface
    private interface IsLikedChecker {
        boolean check(Long photoId);
    }

    private PhotoDto.PhotoListResponse buildPhotoListResponse(Album album, int page, int size,
                                                              IsLikedChecker isLikedChecker) {
        Page<Moment> momentPage = momentRepository.findByAlbumAndStatusOrderByMomentDateDesc(
                album, MomentStatus.ACTIVE, PageRequest.of(page, size));

        List<PhotoDto.MomentResponse> moments = momentPage.getContent().stream()
                .map(moment -> {
                    List<PhotoDto.PhotoSummary> photos = photoRepository
                            .findByMomentAndStatusOrderByUploadDtAsc(moment, PhotoStatus.ACTIVE)
                            .stream()
                            .map(p -> PhotoDto.PhotoSummary.of(p,
                                    photoReactionRepository.countByPhotoIdAndReactionType(
                                            p.getPhotoId(), ReactionType.LIKE),
                                    commentRepository.countByPhotoIdAndStatus(
                                            p.getPhotoId(), CommentStatus.ACTIVE),
                                    isLikedChecker.check(p.getPhotoId())))
                            .toList();
                    return new PhotoDto.MomentResponse(
                            moment.getClientMomentId(),
                            moment.getMomentDate().toString(),
                            moment.getMessage(),
                            photos
                    );
                })
                .toList();

        return new PhotoDto.PhotoListResponse(moments, momentPage.hasNext());
    }

    private Moment createMoment(Album album, String clientMomentId, LocalDate photoDate, String message) {
        return momentRepository.save(
                Moment.builder()
                        .album(album)
                        .clientMomentId(clientMomentId)
                        .momentDate(photoDate)
                        .message(message)
                        .build()
        );
    }

    private Album findActiveAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALBUM_NOT_FOUND));
        if (album.getStatus() != AlbumStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ALBUM_NOT_FOUND);
        }
        return album;
    }

    private AlbumMember findActiveMember(Album album, UserInfo userInfo) {
        return albumMemberRepository
                .findByAlbumAndUserInfoAndStatus(album, userInfo, AlbumMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_ALBUM_MEMBER));
    }

    private void validateGuestMembership(Album album, GuestInfo guestInfo) {
        albumMemberRepository
                .findByAlbumAndGuestInfoAndStatus(album, guestInfo, AlbumMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_ALBUM_MEMBER));
    }

    private UserInfo findUser(Long userId) {
        return userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private GuestInfo findActiveGuest(Long guestId) {
        GuestInfo guest = guestInfoRepository.findById(guestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GUEST_NOT_FOUND));
        if (guest.getStatus() != GuestStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.GUEST_ALREADY_CONVERTED);
        }
        return guest;
    }

    private Photo findActivePhoto(Long photoId) {
        return photoRepository.findByPhotoIdAndStatus(photoId, PhotoStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHOTO_NOT_FOUND));
    }

    private void validatePhotoInAlbum(Photo photo, Long albumId) {
        if (!photo.getAlbum().getAlbumId().equals(albumId)) {
            throw new BusinessException(ErrorCode.PHOTO_NOT_FOUND);
        }
    }
}