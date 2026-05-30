package com.gachiganjik.gachiganjik_server.domain.album.dto;

import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(
        @NotNull AlbumRole role  // ADMIN 또는 MEMBER만 허용
) {}