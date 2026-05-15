package com.haryokuncoro.ops.service;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentService {

    public void processPayment(OrderCreatedEvent event) {
        log.info("Processing payment for order {}", event.orderId());
        simulateDelay();
        log.info("Payment success for order {}", event.orderId());
        //simulate failure
//        throw new RuntimeException(
//                "Payment gateway timeout"
//        );

    }

    private void simulateDelay() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}