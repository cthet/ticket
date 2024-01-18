package com.ticket.mapper;

import com.ticket.dto.TicketDto;
import com.ticket.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source="id", target="id")
    @Mapping(source = "assignedAgent.name", target="assignedAgent")
    TicketDto convertToTicketDto(Ticket ticket);

}
