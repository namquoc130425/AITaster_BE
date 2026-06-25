package com.example.AiTaster.constant;
// enum này dùng để giao dịch này đang làm nghiệp vụ tiền gì
public enum TransactionType {
    PROJECT_ESCROW, // client nạp tiền giữ cho Project
    PROPOSAL_UNLOCK, // mua proposal
    EXPERT_SERVICE,  // clent mua sản phẩm có sẵn
    PAYOUT,// hệ thống trả tiền cho expert
    REFUND // hoàn tiền cho client
}
