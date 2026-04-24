package com.gachiganjik.gachiganjik_server.domain.album.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.album.dto.*;
import com.gachiganjik.gachiganjik_server.domain.album.entity.*;
import com.gachiganjik.gachiganjik_server.domain.album.repository.*;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.PhotoStatus;
import com.gachiganjik.gachiganjik_server.domain.photo.repository.PhotoRepository;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlbumService {

    private static final int ALBUM_MAX_COUNT = 8;
    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;

    private final AlbumRepository albumRepository;
    private final AlbumMemberRepository albumMemberRepository;
    private final AlbumCategoryRepository albumCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final UserInfoRepository userInfoRepository;
    private final PhotoRepository photoRepository;

    public List<AlbumSummaryResponse> getMyAlbums(Long userId) {
        List<AlbumMember> myMemberships = albumMemberRepository.findActiveByUserId(userId);
        return myMemberships.stream()
                .filter(am -> am.getAlbum().getStatus() == AlbumStatus.ACTIVE)
                .map(am -> {
                    int memberCount = albumMemberRepository
                            .findByAlbumAndStatus(am.getAlbum(), AlbumMemberStatus.ACTIVE).size();
                    int photoCount = photoRepository.countByAlbumAndStatus(
                            am.getAlbum().getAlbumId(), PhotoStatus.ACTIVE);
                    return AlbumSummaryResponse.of(am.getAlbum(), am.getRole(), memberCount, photoCount);
                })
                .toList();
    }

    @Transactional
    public AlbumCreateResponse createAlbum(Long userId, AlbumCreateRequest request) {
        UserInfo userInfo = findUser(userId);

        if (albumRepository.countActiveAlbumsByUserId(userId) >= ALBUM_MAX_COUNT) {
            throw new BusinessException(ErrorCode.ALBUM_LIMIT_EXCEEDED);
        }

        validateDateRange(request.eventStartDate(), request.eventEndDate());

        Album album = Album.builder()
                .title(request.title())
                .eventStartDate(parseDate(request.eventStartDate()))
                .eventEndDate(parseDate(request.eventEndDate()))
                .coverImageUrl(request.coverImageUrl())
                .inviteCode(generateUniqueInviteCode())
                .ownerUser(userInfo)
                .build();
        albumRepository.save(album);

        saveCategories(album, request.categories());

        AlbumMember ownerMember = AlbumMember.builder()
                .album(album)
                .userInfo(userInfo)
                .role(AlbumRole.OWNER)
                .build();
        albumMemberRepository.save(ownerMember);

        return AlbumCreateResponse.of(album);
    }

    public AlbumDetailResponse getAlbumDetail(Long userId, Long albumId) {
        Album album = findActiveAlbum(albumId);
        AlbumMember myMember = findActiveMember(album, findUser(userId));
        List<AlbumMember> members = albumMemberRepository.findByAlbumAndStatus(album, AlbumMemberStatus.ACTIVE);
        int photoCount = photoRepository.countByAlbumAndStatus(albumId, PhotoStatus.ACTIVE);
        return AlbumDetailResponse.of(album, myMember.getRole(), members, photoCount);
    }

    @Transactional
    public AlbumSummaryResponse updateAlbum(Long userId, Long albumId, AlbumUpdateRequest request) {
        Album album = findActiveAlbum(albumId);
        AlbumMember myMember = findActiveMember(album, findUser(userId));

        if (myMember.getRole() != AlbumRole.OWNER && myMember.getRole() != AlbumRole.ADMIN) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        validateDateRange(request.eventStartDate(), request.eventEndDate());
        album.update(
                request.title(),
                parseDate(request.eventStartDate()),
                parseDate(request.eventEndDate()),
                request.coverImageUrl()
        );

        if (!CollectionUtils.isEmpty(request.categories())) {
            albumCategoryRepository.deleteByAlbum(album);
            saveCategories(album, request.categories());
        }

        int memberCount = albumMemberRepository.findByAlbumAndStatus(album, AlbumMemberStatus.ACTIVE).size();
        int photoCount = photoRepository.countByAlbumAndStatus(album.getAlbumId(), PhotoStatus.ACTIVE);
        return AlbumSummaryResponse.of(album, myMember.getRole(), memberCount, photoCount);
    }

    @Transactional
    public void deleteAlbum(Long userId, Long albumId) {
        Album album = findActiveAlbum(albumId);
        AlbumMember myMember = findActiveMember(album, findUser(userId));

        if (myMember.getRole() != AlbumRole.OWNER) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        album.delete();
    }

    @Transactional
    public AlbumJoinResponse joinAlbum(Long userId, AlbumJoinRequest request) {
        UserInfo userInfo = findUser(userId);
        Album album = albumRepository
                .findByInviteCodeAndStatus(request.inviteCode(), AlbumStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        if (albumMemberRepository.existsByAlbumAndUserInfoAndStatus(album, userInfo, AlbumMemberStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        AlbumMember member = AlbumMember.builder()
                .album(album)
                .userInfo(userInfo)
                .role(AlbumRole.MEMBER)
                .build();
        albumMemberRepository.save(member);

        return AlbumJoinResponse.of(album, AlbumRole.MEMBER);
    }

    public MemberListResponse getMembers(Long userId, Long albumId) {
        Album album = findActiveAlbum(albumId);
        findActiveMember(album, findUser(userId));
        List<AlbumMember> members = albumMemberRepository.findByAlbumAndStatus(album, AlbumMemberStatus.ACTIVE);
        return MemberListResponse.of(members);
    }

    private void saveCategories(Album album, List<String> categoryNames) {
        if (CollectionUtils.isEmpty(categoryNames)) return;
        List<Category> categories = categoryRepository.findByCategoryNameIn(categoryNames);
        categories.forEach(cat -> albumCategoryRepository.save(
                AlbumCategory.builder().album(album).category(cat).build()
        ));
    }

    private void validateDateRange(String startDate, String endDate) {
        if (startDate == null || endDate == null) return;
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (end.isBefore(start)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private String generateUniqueInviteCode() {
        Random random = new Random();
        String code;
        do {
            code = random.ints(INVITE_CODE_LENGTH, 0, INVITE_CODE_CHARS.length())
                    .mapToObj(i -> String.valueOf(INVITE_CODE_CHARS.charAt(i)))
                    .reduce("", String::concat);
        } while (albumRepository.existsByInviteCode(code));
        return code;
    }

    private LocalDate parseDate(String date) {
        return date != null ? LocalDate.parse(date) : null;
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

    private UserInfo findUser(Long userId) {
        return userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}