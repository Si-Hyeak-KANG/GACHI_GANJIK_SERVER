package com.gachiganjik.gachiganjik_server.domain.album.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AlbumCreateRequest(

        @NotBlank @Size(min = 1, max = 50)
        String title,

        @Size(max = 3, message = "카테고리는 최대 3개까지 선택 가능합니다.")
        List<String> categories,

        String eventStartDate,
        String eventEndDate,
        String coverImageUrl
) {}
