package com.example.AiTaster.service;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.PaymentTransferRequest;
import com.example.AiTaster.dto.request.UserWalletRequest;
import com.example.AiTaster.dto.response.UserWalletResponse;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.UserWalletMapper;
import com.example.AiTaster.repository.UserWalletRepo;
import com.example.AiTaster.service.imp.IUserWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserWalletService implements IUserWalletService {

    private final UserWalletRepo userWalletRepo;
    private final CurrentUserService currentUserService;
    private final UserWalletMapper userWalletMapper;
    private final PaymentTransferService paymentTransferService;

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
    public UserWalletResponse deposit(
            Long walletId,
            BigDecimal amount
    ) {

        UserWallet wallet = getWallet(walletId);

        paymentTransferService.transfer(PaymentTransferRequest.builder()
                .senderId(wallet.getUser().getUserId())
                .receiverId(wallet.getUser().getUserId())
                .sourceWalletId(null)
                .targetWalletId(wallet.getUserWalletId())
                .fromAmount(BigDecimal.ZERO)
                .receiveAmount(amount)
                .transactionType(TransactionType.USER_DEPOSIT)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(wallet.getUserWalletId())
                .paymentReferenceType(PaymentReferenceType.USER_WALLET)
                .providerName("INTERNAL")
                .description("Manual wallet deposit")
                .creditTargetWallet(true)
                .build());

        return userWalletMapper.toResponse(getWallet(walletId));
    }

    @Override
    public UserWalletResponse withdraw(
            Long walletId,
            BigDecimal amount
    ) {

        UserWallet wallet = getWallet(walletId);

        paymentTransferService.transfer(PaymentTransferRequest.builder()
                .senderId(wallet.getUser().getUserId())
                .receiverId(wallet.getUser().getUserId())
                .sourceWalletId(wallet.getUserWalletId())
                .targetWalletId(null)
                .fromAmount(amount)
                .receiveAmount(BigDecimal.ZERO)
                .transactionType(TransactionType.USER_WITHDRAW)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(wallet.getUserWalletId())
                .paymentReferenceType(PaymentReferenceType.WITHDRAW_REQUEST)
                .providerName("INTERNAL")
                .description("Manual wallet withdraw")
                .debitSourceWallet(true)
                .build());

        return userWalletMapper.toResponse(getWallet(walletId));
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
}
