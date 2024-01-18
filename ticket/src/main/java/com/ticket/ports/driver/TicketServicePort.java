package com.ticket.ports.driver;


import com.ticket.dto.TicketDto;
import com.ticket.dto.TicketFilterDto;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketServicePort {
    TicketDto createTicket(TicketDto ticketDto, LocalDateTime localDateTime);

    TicketDto assignAgentToTicket(Long ticketId, Long agentId);

    TicketDto resolveTicket(Long ticketId);

    TicketDto closeTicket(Long ticketId);

    TicketDto updateTicket(Long ticketId, TicketDto ticketDto);

    TicketDto getTicketById(Long ticketId);

    List<TicketDto> getTickets(TicketFilterDto ticketFilterDto);
}
