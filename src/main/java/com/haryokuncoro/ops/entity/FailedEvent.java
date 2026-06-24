package com.haryokuncoro.ops.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "failed_events")
@Getter
@Setter
@NoArgsConstructor
public class FailedEvent extends BaseEntity {
    public enum Status {
        FAILED,
        REPLAYING,
        REPLAYED,
        PERMANENTLY_FAILED
    }

    private String topic;

    private String eventId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer retryCount;

    @Enumerated(EnumType.STRING)
    private Status status;

}