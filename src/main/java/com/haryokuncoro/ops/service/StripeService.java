package com.haryokuncoro.ops.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service @Slf4j
public class StripeService {
    public void transfer(){
        log.info("transfer fund");
    }

    @Transactional
    public void payout(){
        this.transfer();
        log.info("payout fund");
    }
}
