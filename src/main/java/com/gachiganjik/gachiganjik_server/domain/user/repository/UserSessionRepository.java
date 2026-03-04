// UserSessionRepository.java
package com.gachiganjik.gachiganjik_server.domain.user.repository;

import com.gachiganjik.gachiganjik_server.domain.user.entity.UserSessionStatus;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findBySessionToken(String sessionToken);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'LOGGED_OUT', s.logoutDt = CURRENT_TIMESTAMP " +
            "WHERE s.userInfo.userId = :userId AND s.status = :status")
    void logoutAllByUserIdAndStatus(Long userId, UserSessionStatus status);
}