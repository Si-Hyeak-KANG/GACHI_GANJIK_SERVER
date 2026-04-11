package com.gachiganjik.gachiganjik_server.domain.album.dto;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumRole;

public record AlbumJoinResponse(
        String albumId,
        String title,
        String role
) {
    public static AlbumJoinResponse of(Album album, AlbumRole role) {
        return new AlbumJoinResponse(
                String.valueOf(album.getAlbumId()),
                album.getTitle(),
                role.name()
        );
    }
}
