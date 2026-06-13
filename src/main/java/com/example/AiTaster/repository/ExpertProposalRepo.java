package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertProposal;
import com.example.AiTaster.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpertProposalRepo extends JpaRepository<ExpertProposal, Long> {
    // Tìm proposal theo proposalId.
    Optional<ExpertProposal> findExpertProposalByProposalId(Long proposalId);

    // Check expert đã tạo proposal trong jobpost này chưa.
    // IsDeletedFalse nghĩa là chỉ tính proposal chưa bị xóa mềm.
    boolean existsByJobpostAndExpertProfileAndIsDeletedFalse(JobPost jobpost, ExpertProfile expertProfile
    );

    // Lấy proposal của đúng expert trong đúng jobpost.
    // Dùng khi expert muốn xem/sửa proposal của mình.
    List<ExpertProposal> findByJobpostAndIsDeletedFalseOrderByCreateAtDesc(
            JobPost jobpost
    );

    // Lấy tất cả proposal của expert hiện tại.và lấy mới nhất trước
    // Dùng cho màn hình "proposal của tôi".
    List<ExpertProposal> findByExpertProfileAndIsDeletedFalseOrderByCreateAtDesc(
            ExpertProfile expertProfile
    );

;
}
