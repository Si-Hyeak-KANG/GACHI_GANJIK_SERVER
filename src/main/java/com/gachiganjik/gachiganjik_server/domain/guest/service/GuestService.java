package com.gachiganjik.gachiganjik_server.domain.guest.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.album.entity.*;
import com.gachiganjik.gachiganjik_server.domain.album.repository.AlbumMemberRepository;
import com.gachiganjik.gachiganjik_server.domain.album.repository.AlbumRepository;
import com.gachiganjik.gachiganjik_server.domain.guest.dto.GuestDto;
import com.gachiganjik.gachiganjik_server.domain.guest.entity.GuestInfo;
import com.gachiganjik.gachiganjik_server.domain.guest.entity.GuestStatus;
import com.gachiganjik.gachiganjik_server.domain.guest.repository.GuestInfoRepository;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestService {

    private final GuestInfoRepository guestInfoRepository;
    private final AlbumRepository albumRepository;
    private final AlbumMemberRepository albumMemberRepository;

    @Transactional
    public GuestDto.GuestRegisterResponse register(GuestDto.GuestRegisterRequest request) {
        if (guestInfoRepository.existsByGuestKey(request.guestKey())) {
            throw new BusinessException(ErrorCode.GUEST_KEY_ALREADY_EXISTS);
        }

        Album album = albumRepository
                .findByInviteCodeAndStatus(request.inviteCode(), AlbumStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        GuestInfo guest = guestInfoRepository.save(GuestInfo.builder()
                .guestKey(request.guestKey())
                .nickname(request.nickname())
                .build());

        if (!albumMemberRepository.existsByAlbumAndGuestInfoAndStatus(album, guest, AlbumMemberStatus.ACTIVE)) {
            albumMemberRepository.save(AlbumMember.builder()
                    .album(album)
                    .guestInfo(guest)
                    .role(AlbumRole.GUEST)
                    .build());
        }

        return GuestDto.GuestRegisterResponse.of(guest, album.getAlbumId());
    }

    public GuestDto.GuestRestoreResponse restore(GuestDto.GuestRestoreRequest request) {
        GuestInfo guest = guestInfoRepository
                .findByGuestKeyAndStatus(request.guestKey(), GuestStatus.ACTIVE)
                .orElseGet(() -> {
                    GuestInfo g = guestInfoRepository.findByGuestKey(request.guestKey())
                            .orElseThrow(() -> new BusinessException(ErrorCode.GUEST_NOT_FOUND));
                    if (g.getStatus() == GuestStatus.CONVERTED) {
                        throw new BusinessException(ErrorCode.GUEST_ALREADY_CONVERTED);
                    }
                    return g;
                });

        return GuestDto.GuestRestoreResponse.of(guest);
    }

    @Transactional
    public void convertToMember(String guestKey, UserInfo userInfo) {
        guestInfoRepository.findByGuestKeyAndStatus(guestKey, GuestStatus.ACTIVE)
                .ifPresent(guest -> guest.convert(userInfo));
    }
}