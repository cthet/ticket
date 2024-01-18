package com.ticket.demo.unit.service;

import com.ticket.adapters.repository.AgentRepository;
import com.ticket.adapters.repository.TicketRepository;
import com.ticket.dto.TicketDto;
import com.ticket.dto.TicketFilterDto;
import com.ticket.entity.Agent;
import com.ticket.entity.Status;
import com.ticket.entity.Ticket;
import com.ticket.exception.*;
import com.ticket.mapper.TicketMapper;
import com.ticket.service.TicketService;
import com.ticket.util.RealLocalDateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private RealLocalDateTimeProvider realLocalDateTimeProvider;
    @Mock
    private AgentRepository agentRepository;

    @Mock
    private TicketMapper ticketMapper;

    @BeforeEach
    void setup() {
        ticketService = new TicketService(ticketRepository, agentRepository, ticketMapper);
    }

    @Test
    void givenTicketDetails_whenTicketIsCreated_thenCallsRepositorySave() {
        TicketDto ticketDto = new TicketDto(null, "description", null, null, null, null, null);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(new Ticket());


        ticketService.createTicket(ticketDto, realLocalDateTimeProvider.now());

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }


    @Test
    void givenTicketDetails_whenTicketIsCreated_thenSetsStatusAndCreationDate() {
        String description = "description";
        TicketDto ticketDto = new TicketDto(null, description, null, null, null, null, null);

        Ticket savedTicket = new Ticket(1L, description, Status.NEW, realLocalDateTimeProvider.fakeLocalDateTime());
        TicketDto createdTicketDto = new TicketDto(1L, description, Status.NEW, realLocalDateTimeProvider.fakeLocalDateTime(), null, null, null);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketMapper.convertToTicketDto(savedTicket)).thenReturn(createdTicketDto);

        TicketDto createdTicket = ticketService.createTicket(ticketDto, realLocalDateTimeProvider.now());

        assertNotNull(createdTicket);
        assertEquals(description, createdTicket.getDescription());
        assertEquals(Status.NEW, createdTicket.getStatus());
        assertEquals(realLocalDateTimeProvider.fakeLocalDateTime(), createdTicket.getCreatedDate());
    }

    @Test
    void givenTicketWithoutDescription_whenTicketIsCreated_thenThrowException() {
        TicketDto ticketDto = new TicketDto(null, null, null, null, null, null, null);

        assertThrows(MissingDescriptionException.class, () -> ticketService.createTicket(ticketDto, realLocalDateTimeProvider.now()));
    }

    @Test
    void givenNewTicket_whenAssigningAgent_thenStatusIsInProgress() {
        Long ticketId = 1L;
        Long agentId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.NEW, LocalDateTime.now());
        Agent agent = new Agent(agentId, "Agent001");
        Ticket savedTicket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());
        TicketDto savedTicketDto = new TicketDto(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());
        savedTicket.setAssignedAgent(agent);
        TicketDto ticketDto = new TicketDto(null, description, null, null, null, null, null);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        when(ticketMapper.convertToTicketDto(savedTicket)).thenReturn(savedTicketDto);

        TicketDto updatedTicket = ticketService.assignAgentToTicket(ticketId, agentId);

        assertEquals(ticketId, updatedTicket.getId());
        assertEquals(agentId, agent.getId());
        assertEquals(Status.IN_PROGRESS, updatedTicket.getStatus());
    }

    @Test
    void givenNonexistentTicket_whenAssigningAgent_thenThrowException() {
        Long nonExistentTicketId = 999L;
        Long agentId = 1L;

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.assignAgentToTicket(nonExistentTicketId, agentId)
        );
    }

    @Test
    void givenNonexistentAgent_whenAssigningToTicket_thenThrowException() {
        Long ticketId = 999L;
        Long nonExistentAgentId = 1L;
        String ticketDescription = "description";
        Ticket ticket = new Ticket(ticketId, ticketDescription, Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(agentRepository.findById(nonExistentAgentId)).thenReturn(Optional.empty());

        assertThrows(AgentNotFoundException.class,
                () -> ticketService.assignAgentToTicket(ticketId, nonExistentAgentId)
        );
    }

    @Test
    void givenTicketNotInNewState_whenAssigningAgent_thenThrowException() {
        Long ticketId = 1L;
        Long agentId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.assignAgentToTicket(ticketId, agentId)
        );
    }

    @Test
    void givenTicketInProgress_whenResolving_thenStatusIsResolved() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());
        Ticket savedTicket = new Ticket(ticketId, description, Status.RESOLVED, LocalDateTime.now());
        TicketDto savedTicketDto = new TicketDto(ticketId, description, Status.RESOLVED, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketMapper.convertToTicketDto(savedTicket)).thenReturn(savedTicketDto);


        TicketDto updatedTicket = ticketService.resolveTicket(ticketId);

        assertEquals(Status.RESOLVED, updatedTicket.getStatus());
    }

    @Test
    void givenNonexistentTicket_whenResolving_thenThrowException() {
        Long nonExistentTicketId = 99L;

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.resolveTicket(nonExistentTicketId));
    }

    @Test
    void givenTicketNotInProgressState_whenResolving_thenThrowException() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.resolveTicket(ticketId));
    }

    @Test
    void givenResolvedTicketWithSummary_whenClosing_thenStatusIsClosed() {
        Long ticketId = 1L;
        String description = "description";
        String resolutionSummary = "Summary";
        Ticket ticket = new Ticket(ticketId, description, Status.RESOLVED, LocalDateTime.now());
        ticket.setResolutionSummary(resolutionSummary);
        Ticket savedTicket = new Ticket(ticketId, description, Status.CLOSED, LocalDateTime.now());
        savedTicket.setResolutionSummary(resolutionSummary);
        savedTicket.setClosedDate(LocalDateTime.now());
        TicketDto savedTicketDto = new TicketDto(ticketId, description, Status.CLOSED, LocalDateTime.now());
        savedTicketDto.setResolutionSummary(resolutionSummary);
        savedTicketDto.setClosedDate(LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketMapper.convertToTicketDto(savedTicket)).thenReturn(savedTicketDto);

        TicketDto updatedTicket = ticketService.closeTicket(ticketId);

        assertEquals(Status.CLOSED, updatedTicket.getStatus());
    }

    @Test
    void givenNonexistentTicket_whenClosing_thenThrowException() {
        Long nonExistentTicketId = 99L;

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.closeTicket(nonExistentTicketId));
    }

    @Test
    void givenTicketNotInResolvedState_whenClosing_thenThrowException() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.closeTicket(ticketId));
    }

    @Test
    void givenResolvedTicketWithoutSummary_whenClosing_thenThrowException() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.RESOLVED, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(MissingResolutionSummaryException.class,
                () -> ticketService.closeTicket(ticketId)
        );
    }

    @Test
    void givenTicketDescriptionAndResolutionSummary_whenUpdating_thenDescriptionAndResolutionSummaryAreUpdated() {
        Long ticketId = 1L;
        LocalDateTime now = LocalDateTime.now();
        TicketDto ticketDto = new TicketDto(ticketId, "Description", Status.RESOLVED, now, null, null, "Summary");
        Ticket originalTicket = new Ticket(ticketId, ticketDto.getDescription(), Status.RESOLVED, now);
        originalTicket.setResolutionSummary(ticketDto.getResolutionSummary());
        Ticket updatedTicketFromRepo = new Ticket(ticketId, "Updated description", Status.RESOLVED, now);
        updatedTicketFromRepo.setResolutionSummary("Updated summary");
        TicketDto updatedTicketDtoFromRepo = new TicketDto(ticketId, "Updated description", Status.RESOLVED, now);
        updatedTicketDtoFromRepo.setResolutionSummary("Updated summary");


        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(originalTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(updatedTicketFromRepo);
        when(ticketMapper.convertToTicketDto(updatedTicketFromRepo)).thenReturn(updatedTicketDtoFromRepo);

        TicketDto updatedTicket = ticketService.updateTicket(ticketId, ticketDto);

        assertEquals(updatedTicketFromRepo.getDescription(), updatedTicket.getDescription());
        assertEquals(updatedTicketFromRepo.getResolutionSummary(), updatedTicket.getResolutionSummary());
    }

    @Test
    void givenNonexistentTicket_whenUpdating_thenThrowException() {
        Long nonExistentTicketId = 999L;
        String description = "Description";
        String resolutionSummary = "Summary";
        TicketDto ticketDto = new TicketDto(nonExistentTicketId, description, Status.RESOLVED, LocalDateTime.now(), null, null, resolutionSummary);

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.updateTicket(nonExistentTicketId, ticketDto)
        );
    }

    @Test
    void givenClosedTicket_whenUpdating_thenThrowException() {
        Long ticketId = 1L;
        String ticketDescription = "Ticket description";
        String resolutionSummary = "Resolution summary";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.CLOSED, LocalDateTime.now(), null, null, resolutionSummary);
        Ticket ticket = new Ticket(ticketId, ticketDescription, Status.CLOSED, LocalDateTime.now());
        ticket.setResolutionSummary(resolutionSummary);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.updateTicket(ticketId, ticketDto)
        );
    }


    @Test
    void givenValidTicketId_whenGettingTicket_thenReturnTicketDetails() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "description", Status.NEW, LocalDateTime.now());
        TicketDto ticketDto = new TicketDto(ticketId, "description", Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketMapper.convertToTicketDto(ticket)).thenReturn(ticketDto);

        TicketDto ticketFromDB = ticketService.getTicketById(ticketId);

        assertEquals(ticketId, ticketFromDB.getId());
    }

    @Test
    @DisplayName("Given a nonexistent ticket ID, when getting the ticket, then a TicketNotFoundException is thrown")
    void givenNonexistentTicket_whenGettingTicket_thenThrowException() {
        Long nonExistentTicketId = 999L;

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.getTicketById(nonExistentTicketId)
        );
    }

    @Test
    void givenFilterCriteria_whenGettingTickets_thenReturnFilteredTickets() {
        TicketFilterDto filterDto = new TicketFilterDto(List.of(Status.NEW), null, null,null);
        List<Ticket> filteredTickets = List.of(
                new Ticket(1L, "Ticket 1", Status.NEW, LocalDateTime.now()),
                new Ticket(2L, "Ticket 2", Status.NEW, LocalDateTime.now())
        );

        when(ticketRepository.findWithFilters(anyList(), any(), any(), any())).thenReturn(filteredTickets);

        List<TicketDto> retrievedTickets = ticketService.getTickets(filterDto);

        assertEquals(2, retrievedTickets.size());

    }

    @Test
    @DisplayName("Given an invalid date range, when getting tickets, then an InvalidDateRangeException is thrown")
    void givenInvalidDateRange_whenGettingTickets_thenThrowException() {
        TicketFilterDto filterDto = new TicketFilterDto(
                null,
                LocalDateTime.of(2023, 6, 25, 0, 0),
                LocalDateTime.of(1999, 6, 25, 0, 0),
                null);

        assertThrows(InvalidDateRangeException.class, () -> ticketService.getTickets(filterDto));
    }




}
