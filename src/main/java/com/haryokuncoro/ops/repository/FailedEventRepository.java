package com.haryokuncoro.ops.repository;


import com.haryokuncoro.ops.entity.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FailedEventRepository extends JpaRepository<FailedEvent, UUID> {
    Optional<FailedEvent> findByEventId(String eventId);
    List<FailedEvent> findByTopic(String topic);
}