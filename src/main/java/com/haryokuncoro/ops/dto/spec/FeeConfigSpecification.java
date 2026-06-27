package com.haryokuncoro.ops.dto.spec;

import com.haryokuncoro.ops.entity.BillingOrder;
import com.haryokuncoro.ops.entity.FeeConfig;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class FeeConfigSpecification {
    public static Specification<FeeConfig> hasMerchant(UUID merchantId) {
        return (root, query, cb) ->
                cb.equal(root.get("merchant").get("id"), merchantId);
    }

    public static Specification<FeeConfig> hasActive(Boolean active) {
        return (root, query, cb) ->
                cb.equal(root.get("active"), active);
    }
}
