// UserLoginInfoRepository.java
package com.gachiganjik.gachiganjik_server.domain.user.repository;

import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserLoginInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLoginInfoRepository extends JpaRepository<UserLoginInfo, Long> {
    Optional<UserLoginInfo> findByEmailAndLoginType(String email, LoginType loginType);
    boolean existsByEmailAndLoginType(String email, LoginType loginType);
}