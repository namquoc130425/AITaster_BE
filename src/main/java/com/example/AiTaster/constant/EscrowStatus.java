package com.example.AiTaster.constant;

public enum EscrowStatus {
    WAITING_PAYMENT, // Đã tạo escrow record nhưng client chưa thanh toán
    HELD,            // Tiền đã vào hệ thống và đang bị giữ cho project
    RELEASED,        // Đã trả tiền cho expert
    REFUNDED,        // Đã hoàn tiền client
    CANCELED,        // Project/payment hết hạn hoặc bị hủy
    DISPUTED         // Có tranh chấp
}
