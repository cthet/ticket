package com.ticket.dto;

import com.ticket.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketFilterDto {
    private List<Status> status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String assignedAgent;


}
