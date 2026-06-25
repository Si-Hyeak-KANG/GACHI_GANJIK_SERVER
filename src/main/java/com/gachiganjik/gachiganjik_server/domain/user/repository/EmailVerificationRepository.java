package com.gachiganjik.gachiganjik_server.domain.user.repository;

import com.gachiganjik.gachiganjik_server.domain.user.entity.EmailVerificationCode;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerificationCode, String> {
}