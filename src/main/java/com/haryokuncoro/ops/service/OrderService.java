package com.haryokuncoro.ops.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.haryokuncoro.ops.dto.BillingOrderSpecification;
import com.haryokuncoro.ops.dto.CreateOrderRequest;
import com.haryokuncoro.ops.dto.GetOrderResponse;
import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.entity.BillingOrder;
import com.haryokuncoro.ops.entity.Merchant;
import com.haryokuncoro.ops.event.producer.OrderEventPublisher;
import com.haryokuncoro.ops.exception.BadRequestException;
import com.haryokuncoro.ops.exception.NotFoundException;
import com.haryokuncoro.ops.repository.BillingOrderRepository;
import com.haryokuncoro.ops.repository.MerchantRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional @Slf4j
public class OrderService {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final BillingOrderRepository repository;
    private final MerchantRepository merchantRepository;
    private final FeeService feeService;
    private final OrderEventPublisher publisher;

    public OrderService(BillingOrderRepository repository, MerchantRepository merchantRepository, OrderEventPublisher publisher, FeeService feeService) {
        this.repository = repository;
        this.merchantRepository = merchantRepository;
        this.feeService = feeService;
        this.publisher = publisher;
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Page<GetOrderResponse> search(
            UUID merchantId,
            String orderNo,
            String stripePaymentIntentId,
            Pageable pageable) {

        Specification<BillingOrder> spec = null;

        if (merchantId != null) {
            spec = Specification.allOf(
                    spec,
                    BillingOrderSpecification.hasMerchant(merchantId)
            );
        }

        if (orderNo != null && !orderNo.isBlank()) {
            spec = Specification.allOf(
                    spec,
                    BillingOrderSpecification.hasOrderNo(orderNo)
            );
        }

        if (stripePaymentIntentId != null && !stripePaymentIntentId.isBlank()) {
            spec = Specification.allOf(
                    spec,
                    BillingOrderSpecification.hasStripePaymentIntentId(stripePaymentIntentId)
            );
        }

        return repository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    public GetOrderResponse toResponse(BillingOrder order) {
        return GetOrderResponse.builder()
                .id(order.getId())
                .merchantId(order.getMerchant().getId())
                .orderNo(order.getOrderNo())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .paymentStatus(order.getPaymentStatus())
                .paidAt(order.getPaidAt())
                .build();
    }

    public String publishOrder(CreateOrderRequest request) {
        OrderCreatedEvent event = mapper.convertValue(request, OrderCreatedEvent.class);
        String eventId = event.getStripePaymentIntentId() + event.getMerchantId();
        event.setEventId(eventId);
        publisher.publish(event);
        return request.getOrderNo();
    }

    @Transactional
    public void createOrder(OrderCreatedEvent event){
        UUID merchantId = event.getMerchantId();
        String orderNumber = event.getOrderNo();
        BillingOrder existingOrder = repository.findByMerchantIdAndOrderNo(merchantId, orderNumber).orElse(null);

        if (existingOrder != null) {
            if (existingOrder.getAmount().compareTo(event.getAmount()) == 0) {
                log.error(
                        "duplicate event. merchantId {} orderNumber {} amount {}",
                        merchantId,
                        orderNumber,
                        event.getAmount());

                throw new BadRequestException("order already exists");
            }
            existingOrder.setAmount(event.getAmount());
            existingOrder.setPaymentStatus(event.getPaymentStatus());
            repository.save(existingOrder);
            feeService.generateOrderFees(existingOrder);
            return;
        }

        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new NotFoundException("merchant not found"));
        BillingOrder order = mapper.convertValue(event, BillingOrder.class);
        order.setMerchant(merchant);
        repository.save(order);
        feeService.generateOrderFees(order);
    }

}