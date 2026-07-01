package com.example.AiTaster.service;

import com.example.AiTaster.constant.UserWalletStatus;
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

        wallet.setBalance(
                wallet.getBalance().add(amount)
        );

        return userWalletMapper.toResponse(
                userWalletRepo.save(wallet)
        );
    }

    @Override
    public UserWalletResponse withdraw(
            Long walletId,
            BigDecimal amount
    ) {

        UserWallet wallet = getWallet(walletId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new GlobalException(
                    400,
                    "Insufficient balance"
            );
        }

        wallet.setBalance(
                wallet.getBalance().subtract(amount)
        );

        return userWalletMapper.toResponse(
                userWalletRepo.save(wallet)
        );
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

    public UserWallet depositByUserId(Long userId, BigDecimal amount) {
        validateAmount(amount);
        UserWallet wallet = userWalletRepo.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new GlobalException(404, "Wallet not found for user: " + userId));

        if (!UserWalletStatus.ACTIVE.equals(wallet.getStatus())) {
            throw new GlobalException(400, "Wallet is not active");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        return userWalletRepo.save(wallet);
    }

    public UserWallet withdrawByUserId(Long userId, BigDecimal amount) {
        validateAmount(amount);
        UserWallet wallet = userWalletRepo.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new GlobalException(404, "Wallet not found for user: " + userId));

        if (!UserWalletStatus.ACTIVE.equals(wallet.getStatus())) {
            throw new GlobalException(400, "Wallet is not active");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new GlobalException(400, "Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        return userWalletRepo.save(wallet);
    }
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new GlobalException(400, "Amount must not be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Amount must be greater than zero");
        }
    }
}
