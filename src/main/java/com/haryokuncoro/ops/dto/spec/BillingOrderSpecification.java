package com.haryokuncoro.ops.dto.spec;

import com.haryokuncoro.ops.entity.BillingOrder;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class BillingOrderSpecification {

    public static Specification<BillingOrder> hasMerchant(UUID merchantId) {
        return (root, query, cb) ->
                cb.equal(root.get("merchant").get("id"), merchantId);
    }

    public static Specification<BillingOrder> hasOrderNo(String orderNo) {
        return (root, query, cb) ->
                cb.equal(root.get("orderNo"), orderNo);
    }

    public static Specification<BillingOrder> hasStripePaymentIntentId(String paymentIntentId) {
        return (root, query, cb) ->
                cb.equal(root.get("stripePaymentIntentId"), paymentIntentId);
    }
}