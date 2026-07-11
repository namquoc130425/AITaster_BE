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

    // Endpoint submit form checkout của SePay.
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
        // Số tiền VND gửi sang checkout phải là số nguyên.
        String amount = payment.getGrossAmount()
                .setScale(0, RoundingMode.UNNECESSARY)
                .toPlainString();

        // Mã thanh toán nội bộ, bắt buộc unique trong database.
        String paymentCode = payment.getPaymentCode();

        // LinkedHashMap giữ thứ tự field ổn định để FE render form.
        Map<String, String> fields = new LinkedHashMap<>();

        // Số tiền cần thanh toán.
        fields.put("order_amount", amount);
        // Merchant ID lấy từ SePay.
        fields.put("merchant", merchantId);
        fields.put("currency", "VND");
        // Loại giao dịch checkout mua hàng.
        fields.put("operation", "PURCHASE");

        // Mô tả đơn hàng. Dùng paymentCode để webhook match dễ hơn.
        fields.put("order_description", paymentCode);

        // Mã hóa đơn / mã đơn hàng, bắt buộc unique.
        fields.put("order_invoice_number", paymentCode);

        // URL SePay redirect về khi checkout thành công.
        fields.put("success_url", successUrl);

        // URL SePay redirect về khi checkout lỗi.
        fields.put("error_url", errorUrl);

        // URL SePay redirect về khi user hủy checkout.
        fields.put("cancel_url", cancelUrl);

        // Tạo chữ ký từ đúng các field phía trên.
        String signature = signFields(fields);

        // Thêm chữ ký vào form field.
        fields.put("signature", signature);

        // Trả về dữ liệu để FE tạo hidden form và submit sang SePay.
        return SepayCheckoutFormResponse.builder()
                .actionUrl(checkoutUrl)
                .method("POST")
                .fields(fields)
                .build();
    }
    private String signFields(Map<String, String> fields) {
        // Thứ tự field phải đúng yêu cầu SePay, không sort alphabet.
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

            // Bỏ qua field không có giá trị.
            if (value == null || value.isBlank()) {
                continue;
            }

            // Format: field=value
            signedParts.add(field + "=" + value);
        }

        // Ví dụ chuỗi ký:
        // order_amount=100000,merchant=MERCHANT_123,currency=VND,...
        String rawData = String.join(",", signedParts);

        // Ký bằng HMAC-SHA256 rồi encode kết quả sang Base64.
        return hmacSha256Base64(rawData, secretKey);
    }

    private String hmacSha256Base64(String rawData, String secret) {
        try {
            // Tạo signer HMAC-SHA256.
            Mac mac = Mac.getInstance("HmacSHA256");

            // Nạp secret key vào signer.
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            // Ký rawData.
            byte[] digest = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));

            // SePay yêu cầu kết quả HMAC ở dạng Base64.
            return Base64.getEncoder().encodeToString(digest);

        } catch (Exception e) {
            throw new GlobalException(500, "Không thể ký form thanh toán SePay");
        }
    }


}
