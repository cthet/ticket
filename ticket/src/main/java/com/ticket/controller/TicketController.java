package com.ticket.controller;


import com.ticket.dto.TicketDto;
import com.ticket.dto.TicketFilterDto;
import com.ticket.ports.driver.TicketServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private TicketServicePort ticketServicePort;

    public TicketController(TicketServicePort ticketServicePort) {
        this.ticketServicePort = ticketServicePort;
    }

    @PostMapping
    public ResponseEntity<TicketDto> createTicket(@RequestBody TicketDto ticketDto) {
        TicketDto createdTicket = ticketServicePort.createTicket(ticketDto, LocalDateTime.now());
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/agent/{agentId}")
    public ResponseEntity<TicketDto> assignAgent(@PathVariable Long id, @PathVariable Long agentId) {
        TicketDto updatedTicket = ticketServicePort.assignAgentToTicket(id, agentId);
        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<TicketDto> resolveTicket(@PathVariable Long id) {
        TicketDto resolvedTicket = ticketServicePort.resolveTicket(id);
        return new ResponseEntity<>(resolvedTicket, HttpStatus.OK);
    }


    @PutMapping("/{id}/close")
    public ResponseEntity<TicketDto> closeTicket(@PathVariable Long id) {
        TicketDto closedTicket = ticketServicePort.closeTicket(id);
        return new ResponseEntity<>(closedTicket, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketDto> updateTicket(@PathVariable Long id, @RequestBody TicketDto updatedTicketDetails) {
        TicketDto updatedTicket = ticketServicePort.updateTicket(id, updatedTicketDetails);
        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable Long id) {
        TicketDto ticketDto = ticketServicePort.getTicketById(id);
        return ResponseEntity.ok(ticketDto);
    }

    @GetMapping
    public ResponseEntity<List<TicketDto>> getTickets(TicketFilterDto filter) {
        List<TicketDto> tickets = ticketServicePort.getTickets(filter);
        return ResponseEntity.ok(tickets);
    }
}
