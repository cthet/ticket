package com.ticket.adapters.jpa;

import com.ticket.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepositoryJpa extends JpaRepository<Agent, Long> {
}
