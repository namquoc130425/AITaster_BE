package com.example.AiTaster.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SepayWebhookRequest {

    // ID giao dịch do SePay gửi. Có thể dùng để chống xử lý webhook trùng.
    Long id;

    // Tên ngân hàng/cổng thanh toán, ví dụ: Vietcombank.
    String gateway;

    // Thời điểm giao dịch theo SePay, ví dụ: 2024-07-02 11:08:33.
    String transactionDate;

    // Số tài khoản nhận tiền.
    String accountNumber;

    // Tài khoản phụ nếu có, docs có thể gửi chuỗi rỗng.
    String subAccount;

    // Mã nội dung SePay tách ra.
    // Với QR của mình, nếu des = AIT-PROJ-1-XXXX thì code có thể chính là paymentCode.
    String code;

    // Nội dung chuyển khoản đầy đủ.
    // Mình vẫn nên search paymentCode trong field này để chắc ăn.
    String content;

    // in = tiền vào, out = tiền ra.
    String transferType;

    // Mô tả giao dịch từ ngân hàng/SePay.
    String description;

    // Số tiền giao dịch.
    BigDecimal transferAmount;

    // Số dư sau giao dịch.
    BigDecimal accumulated;

    // Mã tham chiếu ngân hàng/SePay. Ưu tiên dùng để chống trùng.
    String referenceCode;


}
