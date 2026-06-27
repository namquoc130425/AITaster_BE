package com.example.AiTaster.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletBalanceRequest {

    BigDecimal amount;

}