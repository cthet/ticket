package com.ticket.adapters.repository;

import com.ticket.entity.Agent;
import com.ticket.ports.repository.AgentRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AgentRepository implements AgentRepositoryPort {
    @Override
    public Optional<Agent> findById(Long agentId) {
        return Optional.empty();
    }
}
