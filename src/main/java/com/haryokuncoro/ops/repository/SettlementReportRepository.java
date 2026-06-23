package com.haryokuncoro.ops.repository;

import com.haryokuncoro.ops.entity.SettlementReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettlementReportRepository extends JpaRepository<SettlementReport, UUID> {
}