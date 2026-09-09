package com.gachiganjik.gachiganjik_server.domain.user.repository;

import com.gachiganjik.gachiganjik_server.domain.user.entity.VerifiedEmail;
import org.springframework.data.repository.CrudRepository;

public interface VerifiedEmailRepository extends CrudRepository<VerifiedEmail, String> {
}