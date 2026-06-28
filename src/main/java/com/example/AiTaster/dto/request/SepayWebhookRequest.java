package com.example.AiTaster.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookRequest {

    // Unix timestamp khi SePay gửi IPN
    Long timestamp;

    // ORDER_PAID hoặc TRANSACTION_VOID
    @JsonProperty("notification_type")
    String notificationType;

    // Thông tin đơn hàng
    OrderInfo order;

    // Thông tin giao dịch
    TransactionInfo transaction;

    // Thông tin khách hàng
    CustomerInfo customer;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderInfo {

        // ID đơn hàng nội bộ của SePay
        String id;

        // Mã đơn hàng bên SePay
        @JsonProperty("order_id")
        String orderId;

        // CAPTURED, CANCELLED, AUTHENTICATION_NOT_NEEDED
        @JsonProperty("order_status")
        String orderStatus;

        // VND
        @JsonProperty("order_currency")
        String orderCurrency;

        // Số tiền đơn hàng
        @JsonProperty("order_amount")
        BigDecimal orderAmount;

        // Mã hóa đơn của hệ thống mình
        // Quan trọng: nên chính là paymentCode của mình
        @JsonProperty("order_invoice_number")
        String orderInvoiceNumber;

        // Dữ liệu tùy chỉnh nếu có
        @JsonProperty("custom_data")
        JsonNode customData;

        @JsonProperty("user_agent")
        JsonNode userAgent;

        @JsonProperty("ip_address")
        String ipAddress;

        @JsonProperty("order_description")
        String orderDescription;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TransactionInfo {

        // ID giao dịch nội bộ của SePay
        String id;

        // CARD, BANK_TRANSFER, ...
        @JsonProperty("payment_method")
        String paymentMethod;

        // Mã giao dịch duy nhất từ SePay
        // Lưu vào providerTransactionCode/providerTransactionId để chống xử lý trùng
        @JsonProperty("transaction_id")
        String transactionId;

        // PAYMENT hoặc REFUND
        @JsonProperty("transaction_type")
        String transactionType;

        // Ví dụ: 2025-09-01 00:00:15
        @JsonProperty("transaction_date")
        String transactionDate;

        // APPROVED hoặc DECLINED
        @JsonProperty("transaction_status")
        String transactionStatus;

        // Số tiền giao dịch
        @JsonProperty("transaction_amount")
        BigDecimal transactionAmount;

        // VND
        @JsonProperty("transaction_currency")
        String transactionCurrency;

        // Các field thẻ, có thì nhận, không có cũng không lỗi
        @JsonProperty("authentication_status")
        String authenticationStatus;

        @JsonProperty("card_number")
        String cardNumber;

        @JsonProperty("card_holder_name")
        String cardHolderName;

        @JsonProperty("card_expiry")
        String cardExpiry;

        @JsonProperty("card_funding_method")
        String cardFundingMethod;

        @JsonProperty("card_brand")
        String cardBrand;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CustomerInfo {

        // ID khách hàng nội bộ của SePay
        String id;

        // ID khách hàng của hệ thống mình nếu lúc tạo đơn có gửi lên
        @JsonProperty("customer_id")
        String customerId;
    }

}
