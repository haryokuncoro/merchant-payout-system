package com.haryokuncoro.ops.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service @Slf4j
public class StripeService {
    public String transfer(){
        log.info("transfer fund");
        return "tf_" + UUID.randomUUID();
    }

    @Transactional
    public String payout(){
        log.info("payout fund");
        return "po_" + UUID.randomUUID();
    }
}
