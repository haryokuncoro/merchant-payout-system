package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void send(OrderCreatedEvent event) {

        log.info(
                "Sending notification for order {}",
                event.orderId()
        );
    }
}