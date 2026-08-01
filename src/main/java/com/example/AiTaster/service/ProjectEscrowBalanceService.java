package com.example.AiTaster.service;

import com.example.AiTaster.constant.EscrowStatus;
import com.example.AiTaster.entity.ProjectEscrow;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProjectEscrowBalanceService {
    private final ProjectEscrowRepo projectEscrowRepo;

    public ProjectEscrow depositByEscrowId(Long escrowId, BigDecimal amount) {
        validateAmount(amount);

        //giống như nạp và rút của ví riêng thì khi xử lý giao dịch sẽ khóa lại . không cho 2 request xữ lý đồng thời
        ProjectEscrow escrow = projectEscrowRepo.findByProjectEscrowIdForUpdate(escrowId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy khoản ký quỹ của dự án: " + escrowId));

        if (!EscrowStatus.WAITING_PAYMENT.equals(escrow.getEscrowStatus())
                && !EscrowStatus.HELD.equals(escrow.getEscrowStatus())) {
            throw new GlobalException(400, "Khoản ký quỹ này không thể nhận thêm tiền");
        }

        escrow.setHeldAmount(escrow.getHeldAmount().add(amount));
        escrow.setEscrowStatus(EscrowStatus.HELD);

        return projectEscrowRepo.save(escrow);
    }

    public ProjectEscrow withdrawByEscrowId(Long escrowId, BigDecimal amount) {
        validateAmount(amount);
     // không cho 2 request xữ lý đồng thời 
        ProjectEscrow escrow = projectEscrowRepo.findByProjectEscrowIdForUpdate(escrowId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy khoản ký quỹ của dự án: " + escrowId));

        if (!EscrowStatus.HELD.equals(escrow.getEscrowStatus())
                && !EscrowStatus.DISPUTED.equals(escrow.getEscrowStatus())) {
            throw new GlobalException(400, "Không thể rút tiền từ khoản ký quỹ này");
        }

        if (escrow.getHeldAmount().compareTo(amount) < 0) {
            throw new GlobalException(400, "Số dư ký quỹ không đủ");
        }

        escrow.setHeldAmount(escrow.getHeldAmount().subtract(amount));

        return projectEscrowRepo.save(escrow);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new GlobalException(400, "Số tiền không được để trống");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Số tiền phải lớn hơn 0");
        }
    }
}
