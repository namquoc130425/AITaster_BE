package com.example.AiTaster.service;

import com.example.AiTaster.Util.PageUtil;
import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.ProductType;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
import com.example.AiTaster.dto.request.ExpertServiceRejectRequest;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.ServiceFile;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertServiceMapper;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.service.imp.IExpertService;
import com.example.AiTaster.specification.ExpertServiceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertProductService implements IExpertService {

    private final ContentManagerService contentManagerService;
    private final ExpertServiceMapper expertServiceMapper;
    private final ExpertServiceRepo expertServiceRepo;
    private final SkillRepo skillRepo;
    private final CurrentUserService currentUserService;
    private final ExpertProfileRepo expertProfileRepo;
    private final CategoryRepo categoryRepo;
    private final LocalFileStorageService localFileStorageService;
    private final ClientProfileRepo clientProfileRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ExpertServiceResponse CreatService(
            ExpertServiceRequest request
    ) {
        validateInputContent(request);

        ExpertProfile expertProfile =
                getCurrentExpertProfile();

        Category category =
                getCategoryByCategoryId(
                        request.getSelectedCategoryId()
                );

        List<Skill> skills =
                getSkillBySkillId(
                        request.getSelectedSkillIds()
                );

        ExpertService expertService =
                expertServiceMapper.toEntity(
                        request,
                        expertProfile
                );

        expertService.setCategory(category);
        expertService.setSkills(skills);

        /*
         * Service mới tạo chỉ ở DRAFT.
         * Admin có thể accept/reject trực tiếp từ DRAFT.
         */
        expertService.setServiceStatus(ServiceStatus.DRAFT);
        expertService.setRejectionReason(null);
        expertService.setSubmittedAt(null);
        expertService.setReviewedAt(null);
        expertService.setReviewedBy(null);
        expertService.setReviewCount(0);

        attachOrUpdateServiceFile(
                expertService,
                request
        );

        ExpertService saved =
                expertServiceRepo.save(expertService);

        notificationService.notifyAdminAiServiceCreated(saved);

        return expertServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpertServiceResponse updateService(
            Long serviceId,
            ExpertServiceRequest request
    ) {
        ExpertProfile expertProfile =
                getCurrentExpertProfile();

        ExpertService expertService =
                getExpertServiceById(serviceId);

        checkAiservice(
                expertService,
                expertProfile
        );

        if (ServiceStatus.DELETED.equals(expertService.getServiceStatus())) {
            throw new GlobalException(ErrorCode.AI_SERVICE_ALREADY_DELETED);
        }

        if (ServiceStatus.PENDING_REVIEW.equals(expertService.getServiceStatus())) {
            throw new GlobalException(ErrorCode.AI_SERVICE_PENDING_REVIEW);
        }

        validateInputContent(request);

        Category category =
                getCategoryByCategoryId(
                        request.getSelectedCategoryId()
                );

        List<Skill> skills =
                getSkillBySkillId(
                        request.getSelectedSkillIds()
                );

        expertServiceMapper.toUpdateEntity(
                request,
                expertService
        );

        expertService.setSkills(skills);
        expertService.setCategory(category);

        /*
         * Nếu service đang OPEN mà Expert sửa nội dung,
         * service phải quay về DRAFT để admin duyệt lại.
         */
        if (ServiceStatus.OPEN.equals(expertService.getServiceStatus())) {
            expertService.setServiceStatus(ServiceStatus.DRAFT);
            expertService.setRejectionReason(null);
            expertService.setReviewedAt(null);
            expertService.setReviewedBy(null);
        }

        /*
         * Nếu service đang REJECTED:
         * giữ nguyên REJECTED.
         * Expert sửa xong phải gọi /resubmit để chuyển sang PENDING_REVIEW.
         */

        attachOrUpdateServiceFile(
                expertService,
                request
        );

        ExpertService saved =
                expertServiceRepo.save(expertService);

        notificationService.notifyAdminAiServiceUpdated(saved);

        return expertServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpertServiceResponse resubmitRejectedService(Long serviceId) {
        ExpertProfile expertProfile =
                getCurrentExpertProfile();

        ExpertService expertService =
                getExpertServiceById(serviceId);

        checkAiservice(
                expertService,
                expertProfile
        );

        if (!ServiceStatus.REJECTED.equals(expertService.getServiceStatus())) {
            throw new GlobalException(ErrorCode.AI_SERVICE_NOT_REJECTED);
        }

        validateServiceReadyForReview(expertService);

        expertService.setServiceStatus(ServiceStatus.PENDING_REVIEW);
        expertService.setSubmittedAt(LocalDateTime.now());
        expertService.setReviewedAt(null);
        expertService.setReviewedBy(null);
        expertService.setRejectionReason(null);
        expertService.setReviewCount(
                expertService.getReviewCount() == null
                        ? 1
                        : expertService.getReviewCount() + 1
        );

        ExpertService saved =
                expertServiceRepo.save(expertService);

        notificationService.notifyAdminAiServiceSubmitted(saved);

        return expertServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpertServiceResponse acceptService(Long serviceId) {
        User admin =
                currentUserService.getCurrentUser();

        checkAdmin(admin);

        ExpertService expertService =
                getExpertServiceById(serviceId);

        if (!isReviewableStatus(expertService.getServiceStatus())) {
            throw new GlobalException(ErrorCode.AI_SERVICE_NOT_REVIEWABLE);
        }

        validateServiceReadyForReview(expertService);

        expertService.setServiceStatus(ServiceStatus.OPEN);
        expertService.setRejectionReason(null);
        expertService.setReviewedAt(LocalDateTime.now());
        expertService.setReviewedBy(admin);

        ExpertService saved =
                expertServiceRepo.save(expertService);

        notificationService.notifyExpertAiServiceAccepted(saved);

        return expertServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpertServiceResponse rejectService(
            Long serviceId,
            ExpertServiceRejectRequest request
    ) {
        User admin =
                currentUserService.getCurrentUser();

        checkAdmin(admin);

        if (request == null
                || request.getRejectionReason() == null
                || request.getRejectionReason().isBlank()) {
            throw new GlobalException(ErrorCode.REJECTION_REASON_REQUIRED);
        }

        contentManagerService.validateKeywordInput(
                request.getRejectionReason()
        );

        ExpertService expertService =
                getExpertServiceById(serviceId);

        if (!isReviewableStatus(expertService.getServiceStatus())) {
            throw new GlobalException(ErrorCode.AI_SERVICE_NOT_REVIEWABLE);
        }

        expertService.setServiceStatus(ServiceStatus.REJECTED);
        expertService.setRejectionReason(
                request.getRejectionReason().trim()
        );
        expertService.setReviewedAt(LocalDateTime.now());
        expertService.setReviewedBy(admin);

        ExpertService saved =
                expertServiceRepo.save(expertService);

        notificationService.notifyExpertAiServiceRejected(saved);

        return expertServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public Void deleteService(Long serviceId) {
        ExpertProfile expertProfile =
                getCurrentExpertProfile();

        ExpertService expertService =
                getExpertServiceById(serviceId);

        checkAiservice(
                expertService,
                expertProfile
        );

        if (ServiceStatus.DELETED.equals(expertService.getServiceStatus())) {
            throw new GlobalException(ErrorCode.AI_SERVICE_ALREADY_DELETED);
        }

        expertService.setServiceStatus(ServiceStatus.DELETED);
        expertServiceRepo.save(expertService);

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpertServiceResponse> getAllMyServiceByOpend() {
        ExpertProfile expertProfile =
                getCurrentExpertProfile();

        return expertServiceRepo
                .findByExpertProfileAndServiceStatusNot(
                        expertProfile,
                        ServiceStatus.DELETED
                )
                .stream()
                .map(expertServiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpertServiceResponse getMyServiceDetail(Long serviceId) {
        ExpertProfile expertProfile =
                getCurrentExpertProfile();

        ExpertService expertService =
                getExpertServiceById(serviceId);

        checkAiservice(
                expertService,
                expertProfile
        );

        if (ServiceStatus.DELETED.equals(expertService.getServiceStatus())) {
            throw new GlobalException(ErrorCode.AI_SERVICE_ALREADY_DELETED);
        }

        return expertServiceMapper.toResponse(expertService);
    }

    @Transactional(readOnly = true)
    public List<ExpertServiceResponse> getMyPurchasedServices() {
        User currentUser =
                currentUserService.getCurrentUser();

        ClientProfile clientProfile =
                clientProfileRepo.findByUser(currentUser)
                        .orElseThrow(() ->
                                new GlobalException(
                                        403,
                                        "Only client can view purchased services"
                                )
                        );

        return paymentTransactionRepo
                .findBySenderIdAndTransactionTypeAndPaymentReferenceTypeAndPaymentStatusOrderByCreateAtDesc(
                        clientProfile.getUser().getUserId(),
                        TransactionType.EXPERT_SERVICE_PURCHASE,
                        PaymentReferenceType.EXPERT_SERVICE,
                        PaymentStatus.SUCCESS
                )
                .stream()
                .map(PaymentTransaction::getReferenceId)
                .distinct()
                .map(expertServiceRepo::findById)
                .flatMap(java.util.Optional::stream)
                .map(expertServiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpertServiceResponse> getAllPublicServices() {
        return expertServiceRepo
                .findByServiceStatus(ServiceStatus.OPEN)
                .stream()
                .map(expertServiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpertServiceResponse> getAllPublicServicesPage(
            ExpertServiceFillerRequest request
    ) {
        Pageable pageable =
                PageUtil.createPageable(request);

        Page<ExpertService> expertServicesPages =
                expertServiceRepo.findAll(
                        ExpertServiceSpecification.filter(request),
                        pageable
                );

        Page<ExpertServiceResponse> responsePage =
                expertServicesPages.map(
                        expertServiceMapper::toResponse
                );

        return PageResponse.fromPage(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpertServiceResponse getPublicServiceDetail(long serviceId) {
        ExpertService expertService =
                getExpertServiceById(serviceId);

        if (!ServiceStatus.OPEN.equals(expertService.getServiceStatus())) {
            throw new GlobalException(ErrorCode.AI_SERVICE_NOT_PUBLIC);
        }

        return expertServiceMapper.toResponse(expertService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpertServiceResponse> getDraftServices() {
        User admin =
                currentUserService.getCurrentUser();

        checkAdmin(admin);

        return expertServiceRepo
                .findByServiceStatus(ServiceStatus.DRAFT)
                .stream()
                .map(expertServiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpertServiceResponse> getReviewQueueServices() {
        User admin =
                currentUserService.getCurrentUser();

        checkAdmin(admin);

        return expertServiceRepo
                .findByServiceStatusInOrderByUpdateAtAsc(
                        List.of(
                                ServiceStatus.DRAFT,
                                ServiceStatus.PENDING_REVIEW
                        )
                )
                .stream()
                .map(expertServiceMapper::toResponse)
                .toList();
    }

    private void validateInputContent(ExpertServiceRequest request) {
        if (request == null) {
            throw new GlobalException(
                    400,
                    "request is required"
            );
        }

        contentManagerService.validateKeywordInput(
                request.getServiceName()
        );

        contentManagerService.validateKeywordInput(
                request.getServiceDescription()
        );

        if (request.getServiceFee() == null
                || request.getServiceFee().signum() <= 0) {
            throw new GlobalException(ErrorCode.SERVICE_FEE_INVALID);
        }
    }

    private void validateServiceReadyForReview(
            ExpertService expertService
    ) {
        if (expertService.getServiceName() == null
                || expertService.getServiceName().isBlank()
                || expertService.getServiceDescription() == null
                || expertService.getServiceDescription().isBlank()) {
            throw new GlobalException(ErrorCode.AI_SERVICE_NOT_READY_FOR_REVIEW);
        }

        if (expertService.getServiceFee() == null
                || expertService.getServiceFee().signum() <= 0) {
            throw new GlobalException(ErrorCode.SERVICE_FEE_INVALID);
        }

        if (expertService.getCategory() == null) {
            throw new GlobalException(ErrorCode.SERVICE_CATEGORY_REQUIRED);
        }

        if (expertService.getSkills() == null
                || expertService.getSkills().isEmpty()) {
            throw new GlobalException(ErrorCode.SERVICE_SKILL_REQUIRED);
        }

        if (expertService.getServiceFile() == null
                || expertService.getServiceFile().getFileContent() == null
                || expertService.getServiceFile().getFileContent().isBlank()
                || expertService.getServiceFile().getProductFile() == null
                || expertService.getServiceFile().getProductFile().isBlank()) {
            throw new GlobalException(ErrorCode.SERVICE_FILE_REQUIRED);
        }
    }

    private void attachOrUpdateServiceFile(
            ExpertService expertService,
            ExpertServiceRequest request
    ) {
        String docUrl =
                localFileStorageService.saveFile(
                        request.getDocFile()
                );

        String sourceUrl =
                localFileStorageService.saveFile(
                        request.getSourceFile()
                );

        ServiceFile serviceFile =
                expertService.getServiceFile();

        if (serviceFile == null) {
            serviceFile =
                    ServiceFile.builder()
                            .productType(ProductType.SOURCE_CODE)
                            .isActive(true)
                            .expertService(expertService)
                            .build();
        }

        if (docUrl != null && !docUrl.isBlank()) {
            serviceFile.setFileContent(docUrl);
        }

        if (sourceUrl != null && !sourceUrl.isBlank()) {
            serviceFile.setProductFile(sourceUrl);
        }

        serviceFile.setExpertService(expertService);
        serviceFile.setIsActive(true);

        expertService.setServiceFile(serviceFile);
    }

    private List<Skill> getSkillBySkillId(
            List<Long> selectedSkillIds
    ) {
        if (selectedSkillIds == null
                || selectedSkillIds.isEmpty()) {
            throw new GlobalException(ErrorCode.SERVICE_SKILL_REQUIRED);
        }

        List<Long> checkSkillIds =
                new ArrayList<>();

        for (Long skillId : selectedSkillIds) {
            if (skillId == null) {
                continue;
            }

            if (skillId <= 0) {
                continue;
            }

            if (!checkSkillIds.contains(skillId)) {
                checkSkillIds.add(skillId);
            }
        }

        if (checkSkillIds.isEmpty()) {
            throw new GlobalException(ErrorCode.SERVICE_SKILL_REQUIRED);
        }

        List<Skill> skills =
                skillRepo.findAllById(checkSkillIds);

        if (skills.size() != checkSkillIds.size()) {
            throw new GlobalException(
                    400,
                    "Some skills not found"
            );
        }

        return skills;
    }

    private Category getCategoryByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new GlobalException(ErrorCode.SERVICE_CATEGORY_REQUIRED);
        }

        return categoryRepo
                .getCategoriesByCategoryId(categoryId)
                .orElseThrow(() ->
                        new GlobalException(
                                400,
                                "Category Not Found"
                        )
                );
    }

    private void checkAiservice(
            ExpertService expertService,
            ExpertProfile expertProfile
    ) {
        if (!expertService
                .getExpertProfile()
                .getExpertProfileId()
                .equals(expertProfile.getExpertProfileId())) {
            throw new GlobalException(
                    403,
                    "You are not owner of this AI service"
            );
        }
    }

    private boolean isReviewableStatus(ServiceStatus serviceStatus) {
        return ServiceStatus.DRAFT.equals(serviceStatus)
                || ServiceStatus.PENDING_REVIEW.equals(serviceStatus);
    }

    private void checkAdmin(User user) {
        if (!Role.ADMIN.equals(user.getRole())) {
            throw new GlobalException(ErrorCode.ONLY_ADMIN_CAN_REVIEW_AI_SERVICE);
        }
    }

    public ExpertProfile getCurrentExpertProfile() {
        User currentUser =
                currentUserService.getCurrentUser();

        return expertProfileRepo
                .findByUser(currentUser)
                .orElseThrow(() ->
                        new GlobalException(
                                400,
                                "User Not Found"
                        )
                );
    }

    public ExpertService getExpertServiceById(Long serviceId) {
        return expertServiceRepo
                .findById(serviceId)
                .orElseThrow(() ->
                        new GlobalException(ErrorCode.AI_SERVICE_NOT_FOUND)
                );
    }
}