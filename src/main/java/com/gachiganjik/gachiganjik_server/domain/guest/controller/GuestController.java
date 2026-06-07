package com.gachiganjik.gachiganjik_server.domain.guest.controller;

import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.domain.guest.dto.GuestDto;
import com.gachiganjik.gachiganjik_server.domain.guest.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<GuestDto.GuestRegisterResponse>> register(
            @Valid @RequestBody GuestDto.GuestRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(guestService.register(request)));
    }

    @PostMapping("/restore")
    public ResponseEntity<ApiResponse<GuestDto.GuestRestoreResponse>> restore(
            @Valid @RequestBody GuestDto.GuestRestoreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(guestService.restore(request)));
    }
}