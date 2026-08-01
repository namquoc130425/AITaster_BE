package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.WalletDepositPaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentTransactionMapperTest {

    private final PaymentTransactionMapper mapper =
            Mappers.getMapper(PaymentTransactionMapper.class);

    @Test
    void mapsGrossAmountToWalletDepositAmount() {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .grossAmount(new BigDecimal("250000"))
                .receiverId(7L)
                .targetWalletId(16L)
                .build();

        WalletDepositPaymentResponse response =
                mapper.toWalletDepositPaymentResponse(transaction, null);

        assertEquals(new BigDecimal("250000"), response.getAmount());
        assertEquals(7L, response.getUserId());
        assertEquals(16L, response.getWalletId());
    }
}
