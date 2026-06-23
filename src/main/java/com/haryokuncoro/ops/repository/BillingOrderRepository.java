package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.BillingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingOrderRepository extends JpaRepository<BillingOrder, UUID> {

    Optional<BillingOrder> findByOrderNo(String orderNo);

    List<BillingOrder> findByMerchantId(UUID merchantId);
}