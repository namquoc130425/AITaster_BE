package com.example.AiTaster.service;

import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.Mac;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SepayGateway {
    @Value("${app.sepay.merchant-id}")
    private String merchantId;

    @Value("${app.sepay.secret-key}")
    private String secretKey;

    // Endpoint submit form của SePay.
    // Ví dụ theo docs: https://pgapi.sepay.vn/v1/checkout/init
    @Value("${app.sepay.checkout-url}")
    private String checkoutUrl;


    @Value("${app.sepay.checkout-api-url}")
    private String successUrl;

    @Value("${app.sepay.return-url}")
    private String errorUrl;

    @Value("${app.sepay.cancel-url}")
    private String cancelUrl;

    @Value("${app.sepay.ipn-url}")
    private String ipnUrl;

    public SepayCheckoutFormResponse createCheckoutForm(PaymentTransaction payment) {
        // Lấy số tiền từ payment.
        // VND không dùng phần thập phân nên ép về số nguyên.
        String amount = payment.getAmount()
                .setScale(0, RoundingMode.UNNECESSARY)
                .toPlainString();

        // Mã thanh toán nội bộ của hệ thống bạn.
        // Mã này nên unique trong database.
        String paymentCode = payment.getPaymentCode();

        // Dùng LinkedHashMap để giữ thứ tự field khi trả về FE.
        Map<String, String> fields = new LinkedHashMap<>();

        // Số tiền cần thanh toán.
        fields.put("order_amount", amount);
        // Merchant ID của bạn trên SePay.
        fields.put("merchant", merchantId);
        fields.put("currency", "VND");
        // Giao dịch thanh toán mua hàng.
        fields.put("operation", "PURCHASE");

        // Mô tả đơn hàng.
        //  dùng paymentCode để dễ check.
        fields.put("order_description", paymentCode);

        // Mã hóa đơn / mã đơn hàng.
        // Bắt buộc phải unique.
        fields.put("order_invoice_number", paymentCode);

        // URL SePay redirect về khi thành công.
        fields.put("success_url", successUrl);

        // URL SePay redirect về khi lỗi.
        fields.put("error_url", errorUrl);

        // URL SePay redirect về khi user hủy.
        fields.put("cancel_url", cancelUrl);

        // Tạo chữ ký theo đúng fields phía trên.
        String signature = signFields(fields);

        // Thêm signature vào form fields.
        fields.put("signature", signature);

        // Trả về cho FE để FE tạo form hidden rồi submit sang SePay.
        return SepayCheckoutFormResponse.builder()
                .actionUrl(checkoutUrl)
                .method("POST")
                .fields(fields)
                .build();
    }
    private String signFields(Map<String, String> fields) {
        // Thứ tự field phải đúng SePay.
        // Không tự sort alphabet.
        List<String> allowedFields = List.of(
                "order_amount",
                "merchant",
                "currency",
                "operation",
                "order_description",
                "order_invoice_number",
                "customer_id",
                "payment_method",
                "success_url",
                "error_url",
                "cancel_url"
        );

        List<String> signedParts = new ArrayList<>();

        for (String field : allowedFields) {
            String value = fields.get(field);

            // Field nào không có thì bỏ qua.
            if (value == null || value.isBlank()) {
                continue;
            }

            // Ghép theo format: field=value
            signedParts.add(field + "=" + value);
        }

        // Chuỗi ký có dạng:
        // order_amount=100000,merchant=MERCHANT_123,currency=VND,...
        String rawData = String.join(",", signedParts);

        // Ký HMAC-SHA256 rồi encode Base64.
        // chữ ký đc tạo từ dữ liệu và secretKey thông qua hmacSha256
        return hmacSha256Base64(rawData, secretKey);
    }

    private String hmacSha256Base64(String rawData, String secret) {
        try {
            // Tạo bộ ký HMAC-SHA256.
            Mac mac = Mac.getInstance("HmacSHA256");

            // Nạp secret key vào bộ ký.
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            // Ký rawData.
            byte[] digest = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));

            // SePay yêu cầu encode kết quả HMAC sang Base64.
            return Base64.getEncoder().encodeToString(digest);

        } catch (Exception e) {
            throw new GlobalException(500, "Cannot sign SePay checkout form");
        }
    }


}
