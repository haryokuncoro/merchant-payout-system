package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.dto.SeedResponse;
import com.haryokuncoro.ops.dto.enums.FeeType;
import com.haryokuncoro.ops.dto.enums.MerchantStatus;
import com.haryokuncoro.ops.entity.FeeConfig;
import com.haryokuncoro.ops.entity.Merchant;
import com.haryokuncoro.ops.repository.FeeConfigRepository;
import com.haryokuncoro.ops.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeedDataService {

    private final MerchantRepository merchantRepository;
    private final FeeConfigRepository feeConfigRepository;

    @Transactional
    public SeedResponse seed() {

        if (merchantRepository.count() > 0) {
            throw new IllegalStateException("Merchant data already exists");
        }

        List<Merchant> merchants = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {

            Merchant merchant = Merchant.builder()
                    .merchantCode("MRC-%03d".formatted(i))
                    .merchantName("Merchant %d".formatted(i))
                    .email("merchant%d@test.com".formatted(i))
                    .phone("0812345678" + i)
                    .status(MerchantStatus.ACTIVE)
                    .build();

            merchants.add(merchant);
        }

        merchantRepository.saveAll(merchants);

        List<FeeConfig> feeConfigs = new ArrayList<>();

        for (Merchant merchant : merchants) {

            feeConfigs.add(
                    createFee(
                            merchant,
                            FeeType.PLATFORM_FEE,
                            BigDecimal.valueOf(2.90)
                    )
            );

            feeConfigs.add(
                    createFee(
                            merchant,
                            FeeType.STRIPE_FEE,
                            BigDecimal.valueOf(1.50)
                    )
            );

            feeConfigs.add(
                    createFee(
                            merchant,
                            FeeType.TAX,
                            BigDecimal.valueOf(0.50)
                    )
            );
        }

        feeConfigRepository.saveAll(feeConfigs);

        return new SeedResponse(
                merchants.size(),
                feeConfigs.size()
        );
    }

    private FeeConfig createFee(
            Merchant merchant,
            FeeType feeType,
            BigDecimal feeValue
    ) {

        return FeeConfig.builder()
                .merchant(merchant)
                .feeType(feeType)
                .feeValue(feeValue)
                .active(true)
                .effectiveFrom(Instant.now())
                .build();
    }
}