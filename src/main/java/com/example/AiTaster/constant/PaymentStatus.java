package com.example.AiTaster.constant;

public enum PaymentStatus {
    PENDING,  // tạo giao dịch, chờ chuyển khoản
    SUCCESS,  // SePay báo nhận tiền hợp lệ
    FAILED,   // sai tiền / sai dữ liệu
    EXPIRED   // quá hạn thanh toán
}
