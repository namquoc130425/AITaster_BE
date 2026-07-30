package com.example.AiTaster.service.imp;

import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.UserWalletRequest;
import com.example.AiTaster.dto.response.PaymentTransactionResponse;
import com.example.AiTaster.dto.response.UserWalletResponse;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;

import java.util.List;

public interface IUserWalletService {

    UserWalletResponse createWallet(UserWalletRequest request);

    UserWalletResponse getMyWallet();

    UserWalletResponse getAdminWalletBalance();

    UserWalletResponse getWalletById(Long walletId);

    List<UserWalletResponse> getAllWallets();

    List<UserWalletResponse> getWithdrawalRequests();

    List<PaymentTransactionResponse> getMyTransactions();

    UserWalletResponse changeStatus(
            Long walletId,
            UserWalletStatus status
    );

    Void deleteWallet(Long walletId);

    UserWallet createdUserWallet(User user);
}
