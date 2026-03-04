// UserInfoRepository.java
package com.gachiganjik.gachiganjik_server.domain.user.repository;

import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByRandomId(String randomId);
}