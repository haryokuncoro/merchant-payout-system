package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.FeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeeConfigRepository extends JpaRepository<FeeConfig, UUID> {
    List<FeeConfig> findByMerchantIdAndActiveTrue(UUID merchantId);
}