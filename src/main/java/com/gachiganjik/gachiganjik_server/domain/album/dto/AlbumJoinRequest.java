package com.gachiganjik.gachiganjik_server.domain.album.dto;

import jakarta.validation.constraints.NotBlank;

public record AlbumJoinRequest(
        @NotBlank String inviteCode
) {}
