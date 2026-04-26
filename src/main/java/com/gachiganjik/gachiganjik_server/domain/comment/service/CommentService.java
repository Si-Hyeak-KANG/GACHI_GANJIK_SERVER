package com.gachiganjik.gachiganjik_server.domain.comment.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.album.entity.*;
import com.gachiganjik.gachiganjik_server.domain.album.repository.AlbumMemberRepository;
import com.gachiganjik.gachiganjik_server.domain.album.repository.AlbumRepository;
import com.gachiganjik.gachiganjik_server.domain.comment.dto.CommentDto;
import com.gachiganjik.gachiganjik_server.domain.comment.entity.*;
import com.gachiganjik.gachiganjik_server.domain.comment.repository.CommentRepository;
import com.gachiganjik.gachiganjik_server.domain.comment.repository.PhotoReactionRepository;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.Photo;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.PhotoStatus;
import com.gachiganjik.gachiganjik_server.domain.photo.repository.PhotoRepository;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private static final int DEFAULT_COMMENT_SIZE = 20;

    private final CommentRepository commentRepository;
    private final PhotoReactionRepository photoReactionRepository;
    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final AlbumMemberRepository albumMemberRepository;
    private final UserInfoRepository userInfoRepository;

    public CommentDto.CommentListResponse getComments(Long userId, Long albumId, Long photoId,
                                                      Long cursor, int size) {
        Album album = findActiveAlbum(albumId);
        findActiveMember(album, findUser(userId));
        Photo photo = findActivePhoto(photoId, albumId);

        int fetchSize = size > 0 ? size : DEFAULT_COMMENT_SIZE;
        // cursor+1 개 조회해서 hasNext 판별
        List<Comment> comments = commentRepository.findByPhotoAndStatusWithCursor(
                photo, CommentStatus.ACTIVE, cursor, PageRequest.of(0, fetchSize + 1));

        boolean hasNext = comments.size() > fetchSize;
        List<Comment> page = hasNext ? comments.subList(0, fetchSize) : comments;

        String nextCursor = hasNext
                ? String.valueOf(page.get(page.size() - 1).getCommentId())
                : null;

        List<CommentDto.CommentInfo> commentInfos = page.stream()
                .map(c -> CommentDto.CommentInfo.of(c, userId))
                .toList();

        return new CommentDto.CommentListResponse(commentInfos, nextCursor, hasNext);
    }

    @Transactional
    public CommentDto.CommentCreateResponse createComment(Long userId, Long albumId, Long photoId,
                                                          CommentDto.CommentCreateRequest request) {
        Album album = findActiveAlbum(albumId);
        UserInfo user = findUser(userId);
        findActiveMember(album, user);
        Photo photo = findActivePhoto(photoId, albumId);

        Comment comment = commentRepository.save(Comment.builder()
                .photo(photo)
                .userInfo(user)
                .commentText(request.content())
                .build());

        return CommentDto.CommentCreateResponse.of(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long albumId, Long photoId, Long commentId) {
        Album album = findActiveAlbum(albumId);
        findActiveMember(album, findUser(userId));
        findActivePhoto(photoId, albumId); // photo 소속 앨범 검증

        Comment comment = commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getUserInfo() == null || !comment.getUserInfo().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
        }

        comment.delete();
    }

    @Transactional
    public CommentDto.ReactionResponse toggleReaction(Long userId, Long albumId, Long photoId,
                                                      CommentDto.ReactionRequest request) {
        Album album = findActiveAlbum(albumId);
        UserInfo user = findUser(userId);
        findActiveMember(album, user);
        Photo photo = findActivePhoto(photoId, albumId);

        ReactionType reactionType = ReactionType.valueOf(request.reactionType());

        Optional<PhotoReaction> existing = photoReactionRepository
                .findByPhotoAndUserInfoAndReactionType(photo, user, reactionType);

        boolean isLiked;
        if (existing.isPresent()) {
            photoReactionRepository.delete(existing.get());
            isLiked = false;
        } else {
            photoReactionRepository.save(PhotoReaction.builder()
                    .photo(photo)
                    .userInfo(user)
                    .reactionType(reactionType)
                    .build());
            isLiked = true;
        }

        int likeCount = photoReactionRepository.countByPhotoIdAndReactionType(photoId, ReactionType.LIKE);
        return new CommentDto.ReactionResponse(isLiked, likeCount);
    }

    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

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

    private UserInfo findUser(Long userId) {
        return userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Photo findActivePhoto(Long photoId, Long albumId) {
        Photo photo = photoRepository.findByPhotoIdAndStatus(photoId, PhotoStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHOTO_NOT_FOUND));
        if (!photo.getAlbum().getAlbumId().equals(albumId)) {
            throw new BusinessException(ErrorCode.PHOTO_NOT_FOUND);
        }
        return photo;
    }
}