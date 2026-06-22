package com.example.AiTaster.constant;

public enum ProjectStatus {
    WAITING_ESCROW,        // vừa tạo, chờ client nạp tiền vào escrow (chưa cho làm)
    ACTIVE,                // escrow đã giữ tiền, project chạy, milestone 1 có thể IN_PROGRESS
    WAITING_CLIENT_REVIEW, // expert đã submit deliverable, chờ client duyệt/yêu cầu sửa
    COMPLETED,             // client đã approve final deliverable, tiền đã release
    CANCELED               // project bị huỷ

}
