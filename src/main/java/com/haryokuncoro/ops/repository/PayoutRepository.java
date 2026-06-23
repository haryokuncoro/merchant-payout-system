package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    Payout findByMerchantIdAndPeriodStartAndPeriodEnd(UUID merchantId, LocalDate periodStart, LocalDate periodEnd);
    boolean existsByMerchantIdAndPeriodStartAndPeriodEnd(
            UUID merchantId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}