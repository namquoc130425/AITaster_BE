package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletBalanceRequest {
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "10000", message = "Số tiền nạp tối thiểu là 10.000đ")
    @DecimalMax(value = "50000000", message = "Số tiền nạp tối đa là 50.000.000đ")
    @Digits(integer = 12, fraction = 0, message = "Số tiền phải là số nguyên, không phải số thập phân")
    BigDecimal amount;


}
