package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID>, JpaSpecificationExecutor<Merchant> {
    boolean existsByMerchantCode(String merchantCode);
    Optional<Merchant> findByMerchantCode(String merchantCode);

}