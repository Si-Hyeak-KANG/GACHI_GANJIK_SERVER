package com.gachiganjik.gachiganjik_server.domain.album.dto;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumRole;

public record AlbumCreateResponse(
        String albumId,
        String title,
        String inviteCode,
        String role
) {
    public static AlbumCreateResponse of(Album album) {
        return new AlbumCreateResponse(
                String.valueOf(album.getAlbumId()),
                album.getTitle(),
                album.getInviteCode(),
                AlbumRole.OWNER.name()
        );
    }
}
