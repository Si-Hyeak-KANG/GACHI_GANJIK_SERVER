package com.gachiganjik.gachiganjik_server.domain.user.repository;

import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserLoginInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLoginInfoRepository extends JpaRepository<UserLoginInfo, Long> {

    Optional<UserLoginInfo> findByEmailAndLoginType(String email, LoginType loginType);
    boolean existsByEmailAndLoginType(String email, LoginType loginType);
    List<UserLoginInfo> findAllByUserInfo(UserInfo userInfo);

    @Query("""
            select l
              from UserLoginInfo l
              join fetch l.userInfo
             where l.providerId = :providerId
               and l.loginType = :loginType
            """)
    Optional<UserLoginInfo> findByProviderIdAndLoginType(@Param("providerId") String providerId,
                                                         @Param("loginType") LoginType loginType);

    Optional<UserLoginInfo> findByUserInfoAndLoginType(UserInfo userInfo, LoginType loginType);
    Optional<UserLoginInfo> findFirstByUserInfo(UserInfo userInfo);
}