package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletBalanceRequest {
    @NotNull(message = "Amount can't be null")
    @DecimalMin(value = "10000", message = "Minimum deposit must be 10.000đ")
    @DecimalMax(value = "50000000", message = "Maximum deposit is 50.000.000đ")
    @Digits(integer = 12, fraction = 0, message = "Amount must be in integer, not decimal")
    BigDecimal amount;


}