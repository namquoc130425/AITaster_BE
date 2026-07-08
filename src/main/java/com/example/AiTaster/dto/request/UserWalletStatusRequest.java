package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.UserWalletStatus;
import lombok.Data;

@Data
public class UserWalletStatusRequest {
    UserWalletStatus status;
}
