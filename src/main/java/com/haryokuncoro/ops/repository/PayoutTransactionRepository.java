package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.PayoutTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayoutTransactionRepository extends JpaRepository<PayoutTransaction, UUID> {

    List<PayoutTransaction> findByPayoutId(UUID payoutId);
}