package com.ticket.dto;


import com.ticket.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto{
    private Long id;
    private String description;
    private Status status;
    private LocalDateTime createdDate;
    private LocalDateTime closedDate;
    private String assignedAgent;
    private String resolutionSummary;


    public TicketDto(Long id, String description, Status status, LocalDateTime createdDate) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdDate = createdDate;
    }
}
