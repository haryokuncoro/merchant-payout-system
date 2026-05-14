package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}