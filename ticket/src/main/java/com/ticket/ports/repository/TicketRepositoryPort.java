package com.ticket.ports.repository;

import com.ticket.entity.Status;
import com.ticket.entity.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepositoryPort {
    Ticket save(Ticket newTicket);

    Optional<Ticket> findById(Long ticketId);

    List<Ticket> findWithFilters(List<Status> statuses,
                                 LocalDateTime startDate,
                                 LocalDateTime endDate,
                                 String assignedAgent);

}
