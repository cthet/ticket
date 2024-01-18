package com.ticket.service;

import com.ticket.adapters.repository.AgentRepository;
import com.ticket.dto.TicketDto;
import com.ticket.dto.TicketFilterDto;
import com.ticket.entity.Agent;
import com.ticket.entity.Status;
import com.ticket.entity.Ticket;
import com.ticket.exception.*;
import com.ticket.mapper.TicketMapper;
import com.ticket.ports.driver.TicketServicePort;
import com.ticket.ports.repository.TicketRepositoryPort;
import com.ticket.util.ErrorMessages;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService implements TicketServicePort {

    private final TicketRepositoryPort ticketRepository;
    private final AgentRepository agentRepository;
    private final TicketMapper ticketMapper;


    public TicketService(TicketRepositoryPort ticketRepository, AgentRepository agentRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.agentRepository = agentRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    public TicketDto createTicket(TicketDto ticketDto, LocalDateTime localDateTime) {
        if (ticketDto.getDescription() == null || ticketDto.getDescription().isEmpty()) {
            throw new MissingDescriptionException(ErrorMessages.DESCRIPTION_REQUIRED);
        }

        Ticket newTicket = new Ticket();
        newTicket.setDescription(ticketDto.getDescription());
        newTicket.setStatus(Status.NEW);
        newTicket.setCreatedDate(localDateTime.now());

        Ticket savedTicket = ticketRepository.save(newTicket);

        return ticketMapper.convertToTicketDto(savedTicket);
    }

    @Override
    public TicketDto assignAgentToTicket(Long ticketId, Long agentId) {
        Ticket existingTicket = getTicket(ticketId);

        if (existingTicket.getStatus() != Status.NEW) {
            throw new InvalidTicketStateException(ErrorMessages.ONLY_NEW_TICKETS_CAN_BE_ASSIGNED_TO_AN_AGENT);
        }

        Agent assignedAgent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(ErrorMessages.AGENT_NOT_FOUND));

        existingTicket.setStatus(Status.IN_PROGRESS);
        existingTicket.setAssignedAgent(assignedAgent);

        Ticket savedTicket = ticketRepository.save(existingTicket);

        return ticketMapper.convertToTicketDto(savedTicket);
    }

    @Override
    public TicketDto resolveTicket(Long ticketId) {
        Ticket existingTicket = getTicket(ticketId);
        if(existingTicket.getStatus() != Status.IN_PROGRESS)
            throw new InvalidTicketStateException(ErrorMessages.ONLY_TICKETS_IN_PROGRESS_CAN_BE_RESOLVED);
        existingTicket.setStatus(Status.RESOLVED);
        Ticket updatedTicket = ticketRepository.save(existingTicket);
        return ticketMapper.convertToTicketDto(updatedTicket);
    };


    @Override
    public TicketDto closeTicket(Long ticketId) {
        Ticket existingTicket = getTicket(ticketId);
        validateTicketBeforeClosing(existingTicket);
        existingTicket.setStatus(Status.CLOSED);
        Ticket updatedTicket = ticketRepository.save(existingTicket);
        return ticketMapper.convertToTicketDto(updatedTicket);
    }


    @Override
    public TicketDto updateTicket(Long ticketId, TicketDto ticketDto) {
        Ticket existingTicket = getTicket(ticketId);
        if(existingTicket.getStatus() == Status.CLOSED)
            throw new InvalidTicketStateException(ErrorMessages.CLOSED_TICKETS_CANNOT_BE_UPDATED);
        existingTicket.setResolutionSummary(ticketDto.getResolutionSummary());
        existingTicket.setDescription(ticketDto.getDescription());
        Ticket updatedTicket = ticketRepository.save(existingTicket);
        return ticketMapper.convertToTicketDto(updatedTicket);
    }

    @Override
    public TicketDto getTicketById(Long ticketId) {
        Ticket ticket = getTicket(ticketId);
        return ticketMapper.convertToTicketDto(ticket);
    }


    @Override
    public List<TicketDto> getTickets(TicketFilterDto ticketFilterDto) {
        if (ticketFilterDto.getStartDate() != null && ticketFilterDto.getEndDate() != null &&
                ticketFilterDto.getEndDate().isBefore(ticketFilterDto.getStartDate())) {
            throw new InvalidDateRangeException(ErrorMessages.INVALID_DATE_RANGE);
        }

        List<Ticket> filteredTickets = ticketRepository.findWithFilters(
                ticketFilterDto.getStatus(),
                ticketFilterDto.getStartDate(),
                ticketFilterDto.getEndDate(),
                ticketFilterDto.getAssignedAgent()
        );

        return filteredTickets.stream()
                .map(ticketMapper::convertToTicketDto)
                .collect(Collectors.toList());
    }

    private void validateTicketBeforeClosing(Ticket existingTicket) {
        if(existingTicket.getStatus() != Status.RESOLVED)
            throw new InvalidTicketStateException(ErrorMessages.ONLY_RESOLVED_TICKETS_CAN_BE_CLOSED);
        if(existingTicket.getResolutionSummary() == null || existingTicket.getResolutionSummary().isEmpty())
            throw new MissingResolutionSummaryException(ErrorMessages.RESOLUTION_SUMMARY_REQUIRED);
    }

    private Ticket getTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));
        return ticket;
    }

}
