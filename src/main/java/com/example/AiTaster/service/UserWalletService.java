package com.example.AiTaster.service;

import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.UserWalletRequest;
import com.example.AiTaster.dto.response.UserWalletResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.UserWalletMapper;
import com.example.AiTaster.repository.UserWalletRepo;
import com.example.AiTaster.service.imp.IUserWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserWalletService implements IUserWalletService {

    private final UserWalletRepo userWalletRepo;
    private final CurrentUserService currentUserService;
    private final UserWalletMapper userWalletMapper;
    private final MoneyMovementService moneyMovementService;

    @Override
    public UserWalletResponse createWallet(UserWalletRequest request) {
        User user = currentUserService.getCurrentUser();

        if (userWalletRepo.findByUser(user).isPresent()) {
            throw new GlobalException(400, "Wallet already exists");
        }

        UserWallet wallet = UserWallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .currency(request.getCurrency())
                .status(UserWalletStatus.ACTIVE)
                .build();

        return userWalletMapper.toResponse(userWalletRepo.save(wallet));
    }

//    @Override
//    public UserWalletResponse createWallet(UserWalletRequest request) {
//
//        User user = currentUserService.getCurrentUser();
//        // Có nên check role?
//        // Trả về thêm Id của profile (xem thử)
//        if (userWalletRepo.findByUser(user).isPresent()) {
//            throw new GlobalException(400, "Wallet already exists");
//        }
//
//        UserWallet wallet = UserWallet.builder()
//                .user(user)
//                .balance(BigDecimal.ZERO)
//                .frozenBalance(BigDecimal.ZERO)
//                .currency(request.getCurrency())
//                .status(UserWalletStatus.ACTIVE)
//                .build();
//
//        return userWalletMapper.toResponse(
//                userWalletRepo.save(wallet)
//        );
//    }


    @Override
    public UserWalletResponse getMyWallet() {

        User user = currentUserService.getCurrentUser();

        UserWallet wallet = userWalletRepo.findByUser(user)
                .orElseThrow(() ->
                        new GlobalException(404, "Wallet not found"));

        return userWalletMapper.toResponse(wallet);
    }

    @Override
    public UserWalletResponse getWalletById(Long walletId) {

        return userWalletMapper.toResponse(
                getWallet(walletId)
        );
    }

    @Override
    public List<UserWalletResponse> getAllWallets() {

        return userWalletRepo.findAll()
                .stream()
                .map(userWalletMapper::toResponse)
                .toList();
    }

    @Override
    public UserWalletResponse changeStatus(
            Long walletId,
            UserWalletStatus status
    ) {

        UserWallet wallet = getWallet(walletId);

        wallet.setStatus(status);

        return userWalletMapper.toResponse(
                userWalletRepo.save(wallet)
        );
    }

    @Override
    public Void deleteWallet(Long walletId) {

        UserWallet wallet = getWallet(walletId);

        userWalletRepo.delete(wallet);

        return null;
    }

    @Override
    public UserWallet createdUserWallet(User user) {
        if (userWalletRepo.findByUser(user).isPresent()) {
            throw new GlobalException(400, "User already has wallet");
        }
        UserWallet wallet = UserWallet.builder()
                .user(user)
                .status(UserWalletStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .frozenBalance(BigDecimal.ZERO)
                .build();
        return userWalletRepo.save(wallet);
    }

    public UserWallet createWalletIfAbsent(User user) {
        return userWalletRepo.findByUser(user)
                .orElseGet(() -> createdUserWallet(user));
    }

    private UserWallet getWallet(Long walletId) {

        return userWalletRepo.findById(walletId)
                .orElseThrow(() ->
                        new GlobalException(
                                404,
                                "Wallet not found"
                        ));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new GlobalException(400, "Amount must not be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Amount must be greater than zero");
        }
    }
    //hàm yêu cầu rút tiền . set setRequestWithdrawal true và truyền số tiền yêu cau rút về cho admin
    @Transactional
    public UserWalletResponse requestWithdraw(Long walletId, BigDecimal amount) {
        validateAmount(amount);

        User currentUser = currentUserService.getCurrentUser();
        UserWallet wallet = getWallet(walletId);

        if (!wallet.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new GlobalException(403, "You are not owner of this wallet");
        }

        if (!UserWalletStatus.ACTIVE.equals(wallet.getStatus())) {
            throw new GlobalException(400, "Wallet is not active");
        }

        if (Boolean.TRUE.equals(wallet.getRequestWithdrawal())) {
            throw new GlobalException(400, "Withdrawal request already exists");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new GlobalException(400, "Insufficient balance");
        }

        wallet.setRequestWithdrawal(true);
        wallet.setAmountRequestWithdrawal(amount);

        return userWalletMapper.toResponse(userWalletRepo.save(wallet));
    }

    @Transactional
    public PaymentTransaction approveWithdraw(Long walletId) {
        UserWallet wallet = getWallet(walletId);

        if (!Boolean.TRUE.equals(wallet.getRequestWithdrawal())) {
            throw new GlobalException(400, "No withdrawal request");
        }

        BigDecimal amount = wallet.getAmountRequestWithdrawal();

        PaymentTransaction transaction = moneyMovementService.moneyTransactionManagement(
                wallet.getUser().getUserId(),
                null,
                TransactionType.USER_WITHDRAW,
                wallet.getUserWalletId(),
                PaymentReferenceType.WITHDRAW_REQUEST,
                "Approve wallet withdrawal - wallet " + wallet.getUserWalletId(),
                amount,
                BigDecimal.ZERO,
                null
        );

        wallet.setRequestWithdrawal(false);
        wallet.setAmountRequestWithdrawal(BigDecimal.ZERO);
        userWalletRepo.save(wallet);

        return transaction;
    }
}
