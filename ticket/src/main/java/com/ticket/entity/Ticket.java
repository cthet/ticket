package com.ticket.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdDate;

    private LocalDateTime closedDate;

    private String resolutionSummary;
    @ManyToOne
    @JoinColumn(name = "assignedAgentId")
    private Agent assignedAgent;


    public Ticket(Long id, String description, Status status, LocalDateTime createdDate) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdDate = createdDate;
    }

}
