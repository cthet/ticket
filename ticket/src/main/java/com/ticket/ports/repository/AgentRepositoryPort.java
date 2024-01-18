package com.ticket.ports.repository;

import com.ticket.entity.Agent;

import java.util.Optional;

public interface AgentRepositoryPort {
    Optional<Agent> findById(Long agentId);
}
