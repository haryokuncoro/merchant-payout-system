package com.haryokuncoro.ops.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "failed_events")
@Getter
@Setter
@NoArgsConstructor
public class FailedEvent {

    @Id
    private UUID id;

    private String topic;

    private UUID eventId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String status;

    private Instant createdAt;
}