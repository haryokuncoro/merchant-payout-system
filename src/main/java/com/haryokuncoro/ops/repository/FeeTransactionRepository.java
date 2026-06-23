package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.FeeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeeTransactionRepository extends JpaRepository<FeeTransaction, UUID> {

    List<FeeTransaction> findByOrderId(UUID orderId);
}
