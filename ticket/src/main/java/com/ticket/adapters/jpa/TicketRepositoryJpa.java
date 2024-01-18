package com.ticket.adapters.jpa;

import com.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TicketRepositoryJpa extends JpaRepository<Ticket, Long> {

}
