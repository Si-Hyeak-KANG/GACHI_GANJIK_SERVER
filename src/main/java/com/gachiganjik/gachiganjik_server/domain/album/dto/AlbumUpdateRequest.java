package com.gachiganjik.gachiganjik_server.domain.album.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record AlbumUpdateRequest(

        @Size(min = 1, max = 50)
        String title,

        @Size(max = 3)
        List<String> categories,

        String eventStartDate,
        String eventEndDate,
        String coverImageUrl
) {}
