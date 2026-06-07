package com.gachiganjik.gachiganjik_server.domain.guest.dto;

import com.gachiganjik.gachiganjik_server.domain.guest.entity.GuestInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GuestDto {

    public record GuestRegisterRequest(
            @NotBlank @Size(min = 4, max = 20) String guestKey,
            @NotBlank @Size(min = 1, max = 50) String nickname,
            @NotBlank String inviteCode
    ) {}

    public record GuestRestoreRequest(
            @NotBlank String guestKey
    ) {}

    public record GuestRegisterResponse(
            String guestId,
            String guestKey,
            String nickname,
            String albumId
    ) {
        public static GuestRegisterResponse of(GuestInfo guest, Long albumId) {
            return new GuestRegisterResponse(
                    String.valueOf(guest.getGuestId()),
                    guest.getGuestKey(),
                    guest.getNickname(),
                    String.valueOf(albumId)
            );
        }
    }

    public record GuestRestoreResponse(
            String guestId,
            String guestKey,
            String nickname
    ) {
        public static GuestRestoreResponse of(GuestInfo guest) {
            return new GuestRestoreResponse(
                    String.valueOf(guest.getGuestId()),
                    guest.getGuestKey(),
                    guest.getNickname()
            );
        }
    }
}