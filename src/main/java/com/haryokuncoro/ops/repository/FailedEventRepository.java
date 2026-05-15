package com.haryokuncoro.ops.repository;


import com.haryokuncoro.ops.entity.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FailedEventRepository extends JpaRepository<FailedEvent, UUID> {
}