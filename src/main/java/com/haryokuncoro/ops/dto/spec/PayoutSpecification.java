package com.haryokuncoro.ops.dto.spec;

import com.haryokuncoro.ops.entity.Payout;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class PayoutSpecification {
    public static Specification<Payout> hasMerchant(UUID merchantId) {
        return (root, query, cb) ->
                cb.equal(root.get("merchant").get("id"), merchantId);
    }
}
