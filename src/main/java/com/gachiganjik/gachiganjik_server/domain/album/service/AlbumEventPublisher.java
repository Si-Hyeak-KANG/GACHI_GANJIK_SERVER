package com.gachiganjik.gachiganjik_server.domain.album.service;

import com.gachiganjik.gachiganjik_server.domain.album.dto.AlbumEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishPhotoUploaded(Long albumId, String uploaderNickname, int photoCount, LocalDateTime uploadedAt) {
        AlbumEventPayload payload = AlbumEventPayload.photoUploaded(
                String.valueOf(albumId), uploaderNickname, photoCount, uploadedAt);
        String destination = "/topic/albums/" + albumId;
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("[WS] Published PHOTO_UPLOADED → {} by {}", destination, uploaderNickname);
    }
}