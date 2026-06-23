package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.FeeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface FeeTransactionRepository extends JpaRepository<FeeTransaction, UUID> {
    void deleteByOrderId(UUID orderId);
    List<FeeTransaction> findByOrderId(UUID orderId);
    @Query("""
       select coalesce(sum(ft.amount),0)
       from FeeTransaction ft
       where ft.order.id = :orderId
       """)
    BigDecimal sumFeeByOrderId(UUID orderId);
}
