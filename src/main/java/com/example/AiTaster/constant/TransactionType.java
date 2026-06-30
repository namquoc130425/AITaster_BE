package com.example.AiTaster.constant;
// enum này dùng để giao dịch này đang làm nghiệp vụ tiền gì
public enum TransactionType {
    PROJECT_ESCROW, // client nạp tiền giữ cho Project

    USER_DEPOSIT,          // user nạp tiền vào ví riêng
    USER_WITHDRAW,         // user rút tiền khỏi ví riêng

    PROJECT_ESCROW_DEPOSIT, // client thanh toán project, tiền vào escrow
    PROJECT_ESCROW_RELEASE, // project xong, escrow trả tiền cho expert
    PROJECT_ESCROW_REFUND,         // hoàn tiền project cho client

    PROPOSAL_PURCHASE,      // client mua proposal / unlock proposal
    EXPERT_SERVICE_PURCHASE,// client mua AI service/product

    PLATFORM_FEE            // phí sàn chuyển về ví admin
}
