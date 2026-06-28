package com.example.AiTaster.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PlatformFeeCalculator {
    @Value("${app.platform.fee-percent:10}")
    private BigDecimal platformFeePercent;

    public BigDecimal calculatePlatformFee(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount
                .multiply(platformFeePercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateExpertAmount(BigDecimal amount) {
        BigDecimal platformFee = calculatePlatformFee(amount);
        return amount.subtract(platformFee);
    }
}
