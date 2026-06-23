package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    Optional<Payout> findByPayoutNo(String payoutNo);
}