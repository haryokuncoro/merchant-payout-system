package com.haryokuncoro.ops.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {

    @Id
    private UUID id;

    private String email;
    private String phone;
    private String password;
    private boolean enabled;
}