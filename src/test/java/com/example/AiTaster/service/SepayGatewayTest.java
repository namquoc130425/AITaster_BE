package com.example.AiTaster.service;

import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SepayGatewayTest {

    @Test
    void createCheckoutForm_addsPaymentCodeToCancelUrl() {
        SepayGateway gateway = new SepayGateway();
        ReflectionTestUtils.setField(gateway, "merchantId", "merchant-test");
        ReflectionTestUtils.setField(gateway, "secretKey", "secret-test");
        ReflectionTestUtils.setField(gateway, "checkoutUrl", "https://sepay.test/checkout");
        ReflectionTestUtils.setField(gateway, "successUrl", "https://app.test/payment/success");
        ReflectionTestUtils.setField(gateway, "errorUrl", "https://app.test/payment/error");
        ReflectionTestUtils.setField(
                gateway,
                "cancelUrl",
                "https://app.test/payment/cancel?source=sepay"
        );

        PaymentTransaction payment = PaymentTransaction.builder()
                .grossAmount(BigDecimal.valueOf(100_000))
                .paymentCode("AIT-PAY-1-ABC12345")
                .build();

        SepayCheckoutFormResponse response = gateway.createCheckoutForm(payment);

        assertThat(response.getFields().get("cancel_url"))
                .isEqualTo(
                        "https://app.test/payment/cancel?source=sepay&paymentCode=AIT-PAY-1-ABC12345"
                );
    }
}
