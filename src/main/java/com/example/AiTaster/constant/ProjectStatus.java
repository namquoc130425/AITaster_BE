package com.example.AiTaster.constant;

public enum ProjectStatus {
    WAITING_ESCROW, // expert accept rồi, chờ client thanh toán escrow
    ACTIVE,         // client đã thanh toán, project bắt đầu chạy
    COMPLETED,      // project hoàn thành
    CANCELED        // project bị hủy hoặc quá hạn thanh toán

}
