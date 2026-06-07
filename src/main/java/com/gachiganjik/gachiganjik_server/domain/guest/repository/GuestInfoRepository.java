package com.gachiganjik.gachiganjik_server.domain.guest.repository;

import com.gachiganjik.gachiganjik_server.domain.guest.entity.GuestInfo;
import com.gachiganjik.gachiganjik_server.domain.guest.entity.GuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestInfoRepository extends JpaRepository<GuestInfo, Long> {

    boolean existsByGuestKey(String guestKey);

    Optional<GuestInfo> findByGuestKey(String guestKey);

    Optional<GuestInfo> findByGuestKeyAndStatus(String guestKey, GuestStatus status);
}