package com.gachiganjik.gachiganjik_server.domain.user.repository;

import com.gachiganjik.gachiganjik_server.domain.user.entity.LinkTicket;
import org.springframework.data.repository.CrudRepository;

public interface LinkTicketRepository extends CrudRepository<LinkTicket, String> {
}