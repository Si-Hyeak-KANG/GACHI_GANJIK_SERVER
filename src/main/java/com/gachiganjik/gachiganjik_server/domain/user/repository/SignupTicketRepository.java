package com.gachiganjik.gachiganjik_server.domain.user.repository;

import com.gachiganjik.gachiganjik_server.domain.user.entity.SignupTicket;
import org.springframework.data.repository.CrudRepository;

public interface SignupTicketRepository extends CrudRepository<SignupTicket, String> {
}