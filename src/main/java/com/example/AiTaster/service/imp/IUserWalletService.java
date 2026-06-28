package com.example.AiTaster.service.imp;

import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.UserWalletRequest;
import com.example.AiTaster.dto.response.UserWalletResponse;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;

import java.math.BigDecimal;
import java.util.List;

public interface IUserWalletService {

    UserWalletResponse createWallet(UserWalletRequest request);

    UserWalletResponse getMyWallet();

    UserWalletResponse getWalletById(Long walletId);

    List<UserWalletResponse> getAllWallets();

    UserWalletResponse deposit(Long walletId, BigDecimal amount);

    UserWalletResponse withdraw(Long walletId, BigDecimal amount);

    UserWalletResponse changeStatus(
            Long walletId,
            UserWalletStatus status
    );

    Void deleteWallet(Long walletId);

    UserWallet createdUserWallet(User user);
}
