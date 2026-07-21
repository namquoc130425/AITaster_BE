package com.example.AiTaster.service;

import com.example.AiTaster.Util.PageUtil;
<<<<<<< HEAD
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.ProductType;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertServiceMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.imp.IExpertService;
import com.example.AiTaster.specification.ExpertServiceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
=======
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
import com.example.AiTaster.dto.response.InvoiceResponse;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

@Service
@RequiredArgsConstructor
public class ExpertProductService implements IExpertService {
<<<<<<< HEAD
=======

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    private final ContentManagerService contentManagerService;
    private final ExpertServiceMapper expertServiceMapper;
    private final ExpertServiceRepo expertServiceRepo;
    private final SkillRepo skillRepo;
    private final CurrentUserService currentUserService;
    private final ExpertProfileRepo expertProfileRepo;
    private final CategoryRepo categoryRepo;
<<<<<<< HEAD
    private final ServiceFileRepo serviceFileRepo;
    private final LocalFileStorageService localFileStorageService;
    private final ClientProfileRepo clientProfileRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;

    @Override
    public ExpertServiceResponse CreatService(
            ExpertServiceRequest request
    ) {

        validateInputContent(request);

        ExpertProfile expertProfile =
                getCurrentExpertProfile();
=======
    private final LocalFileStorageService localFileStorageService;
    private final ClientProfileRepo clientProfileRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final NotificationService notificationService;
    private final ExpertVerificationGuardService expertVerificationGuardService;

    public record ServiceFileDownload(
            Resource resource,
            String fileName,
            String contentType,
            long contentLength
    ) {}

    @Override
    @Transactional
    public ExpertServiceResponse CreatService(
            ExpertServiceRequest request
    ) {
        ExpertProfile expertProfile =
                getCurrentExpertProfile();
        ensureExpertVerified(expertProfile);

        validateInputContent(request);
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

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
<<<<<<< HEAD

=======
        expertService.setServiceStatus(ServiceStatus.PENDING_REVIEW);
        expertService.setRejectionReason(null);
        expertService.setSubmittedAt(LocalDateTime.now());
        expertService.setReviewedAt(null);
        expertService.setReviewedBy(null);
        expertService.setReviewCount(1);

        attachOrUpdateServiceFile(
                expertService,
                request
        );

        ExpertService saved =
                expertServiceRepo.save(expertService);

        notificationService.notifyAdminAiServiceSubmitted(saved);

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

        if (ServiceStatus.OPEN.equals(expertService.getServiceStatus())
                || ServiceStatus.DRAFT.equals(expertService.getServiceStatus())) {
            expertService.setServiceStatus(ServiceStatus.PENDING_REVIEW);
            expertService.setSubmittedAt(LocalDateTime.now());
            expertService.setRejectionReason(null);
            expertService.setReviewedAt(null);
            expertService.setReviewedBy(null);
            expertService.setReviewCount(
                    expertService.getReviewCount() == null
                            ? 1
                            : expertService.getReviewCount() + 1
            );
        }

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
                .findByExpertProfile_ExpertProfileIdAndServiceStatusNot(
                        expertProfile.getExpertProfileId(),
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
                .map(paymentTransaction ->
                        expertServiceRepo.findById(paymentTransaction.getReferenceId())
                                .map(expertService -> toPurchasedServiceResponse(expertService, paymentTransaction))
                                .orElse(null)
                )
                .filter(Objects::nonNull)
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

    @Transactional(readOnly = true)
    public ServiceFileDownload downloadServiceFile(Long serviceId, Long serviceFileId, String kind) {
        ExpertService expertService =
                getExpertServiceById(serviceId);
        ServiceFile serviceFile =
                expertService.getServiceFile();

        if (serviceFile == null
                || !serviceFileId.equals(serviceFile.getServiceFileId())) {
            throw new GlobalException(404, "Service file not found");
        }

        checkCanDownloadServiceFile(expertService);

        String requestedKind = kind == null || kind.isBlank()
                ? "product"
                : kind.trim().toLowerCase();
        String filePathValue = switch (requestedKind) {
            case "instruction" -> serviceFile.getFileContent();
            case "product" -> serviceFile.getProductFile();
            default -> throw new GlobalException(400, "Unsupported service file kind");
        };

        Path filePath =
                resolveLocalUploadPath(filePathValue);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new GlobalException(404, "Service file not found on server");
        }

        try {
            Resource resource =
                    new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new GlobalException(404, "Service file is not readable");
            }

            String contentType =
                    Files.probeContentType(filePath);
            return new ServiceFileDownload(
                    resource,
                    resolveDownloadFileName(filePathValue),
                    contentType != null ? contentType : "application/octet-stream",
                    Files.size(filePath)
            );
        } catch (GlobalException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GlobalException(500, "Cannot download service file");
        }
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

    private void checkCanDownloadServiceFile(ExpertService expertService) {
        User currentUser =
                currentUserService.getCurrentUser();
        if (Role.ADMIN.equals(currentUser.getRole())) {
            return;
        }

        boolean isExpertOwner =
                expertProfileRepo.findByUser(currentUser)
                        .map(expertProfile -> expertService.getExpertProfile() != null
                                && expertProfile.getExpertProfileId().equals(
                                expertService.getExpertProfile().getExpertProfileId()
                        ))
                        .orElse(false);
        boolean isPurchasedClient =
                paymentTransactionRepo
                        .existsBySenderIdAndTransactionTypeAndPaymentReferenceTypeAndPaymentStatusAndReferenceId(
                                currentUser.getUserId(),
                                TransactionType.EXPERT_SERVICE_PURCHASE,
                                PaymentReferenceType.EXPERT_SERVICE,
                                PaymentStatus.SUCCESS,
                                expertService.getServiceId()
                        );

        if (!isExpertOwner && !isPurchasedClient) {
            throw new GlobalException(403, "You cannot download this service file");
        }
    }

    private ExpertServiceResponse toPurchasedServiceResponse(
            ExpertService expertService,
            PaymentTransaction paymentTransaction
    ) {
        ExpertServiceResponse response =
                expertServiceMapper.toResponse(expertService);
        response.setPaymentTransactionId(paymentTransaction.getPaymentTransactionId());
        response.setReceivedAt(
                paymentTransaction.getPaidAt() != null
                        ? paymentTransaction.getPaidAt()
                        : paymentTransaction.getCreateAt()
        );
        response.setInvoice(toInvoiceResponse(paymentTransaction));
        return response;
    }

    private InvoiceResponse toInvoiceResponse(PaymentTransaction paymentTransaction) {
        BigDecimal subtotal = paymentTransaction.getGrossAmount() != null
                ? paymentTransaction.getGrossAmount()
                : BigDecimal.ZERO;
        BigDecimal fee = paymentTransaction.getFeeAmount() != null
                ? paymentTransaction.getFeeAmount()
                : BigDecimal.ZERO;

        return InvoiceResponse.builder()
                .invoiceId(paymentTransaction.getPaymentTransactionId())
                .invoiceCode(paymentTransaction.getPaymentCode())
                .invoiceType(paymentTransaction.getPaymentReferenceType().name())
                .invoiceStatus(paymentTransaction.getPaymentStatus().name())
                .subtotalAmount(subtotal)
                .platformFee(fee)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(subtotal)
                .currency(paymentTransaction.getCurrency())
                .paymentMethod(paymentTransaction.getPaymentMethod().name())
                .description(paymentTransaction.getDescription())
                .paidAt(paymentTransaction.getPaidAt())
                .createAt(paymentTransaction.getCreateAt())
                .build();
    }

    private Path resolveLocalUploadPath(String filePathValue) {
        if (filePathValue == null || filePathValue.isBlank()) {
            throw new GlobalException(404, "Service file path is empty");
        }

        if (filePathValue.matches("(?i)^https?://.*")) {
            throw new GlobalException(400, "External service file is not supported");
        }

        String relativePath =
                filePathValue.replace('\\', '/');
        while (relativePath.startsWith("/")) {
            relativePath =
                    relativePath.substring(1);
        }

        Path uploadsRoot =
                Path.of("uploads").toAbsolutePath().normalize();
        Path filePath =
                Path.of(relativePath).toAbsolutePath().normalize();

        if (!filePath.startsWith(uploadsRoot)) {
            throw new GlobalException(403, "Invalid service file path");
        }

        return filePath;
    }

    private String resolveDownloadFileName(String filePathValue) {
        String fileName =
                filePathValue.replace('\\', '/');
        fileName =
                fileName.substring(fileName.lastIndexOf('/') + 1);

        int separatorIndex =
                fileName.indexOf("_");
        if (separatorIndex >= 0 && separatorIndex + 1 < fileName.length()) {
            return fileName.substring(separatorIndex + 1);
        }

        return fileName.isBlank() ? "service-file" : fileName;
    }

    private void attachOrUpdateServiceFile(
            ExpertService expertService,
            ExpertServiceRequest request
    ) {
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        String docUrl =
                localFileStorageService.saveFile(
                        request.getDocFile()
                );

        String sourceUrl =
                localFileStorageService.saveFile(
                        request.getSourceFile()
                );

<<<<<<< HEAD
        ServiceFile serviceFile = //use mapper
                ServiceFile.builder()
                        .fileContent(docUrl)
                        .productFile(sourceUrl)
                        .productType(
                                ProductType.SOURCE_CODE
                        )
                        .isActive(true)
                        .expertService(expertService)
                        .build();

        expertService.setServiceFile(
                serviceFile
        );

        ExpertService saved =
                expertServiceRepo.save(
                        expertService
                );

        return expertServiceMapper.toResponse(
                saved
        );
    }

    @Override
    public ExpertServiceResponse updateService(Long serviceId, ExpertServiceRequest expertServiceRequest) {

        ExpertProfile expertProfile = getCurrentExpertProfile();
        ExpertService expertService = getExpertServiceById(serviceId);
        checkAiservice(expertService, expertProfile);
        validateInputContent(expertServiceRequest);
        Category category = getCategoryByCategoryId(expertServiceRequest.getSelectedCategoryId());
        List<Skill> skills = getSkillBySkillId(expertServiceRequest.getSelectedSkillIds());
        expertServiceMapper.toUpdateEntity(expertServiceRequest,expertService);
        expertService.setSkills(skills);
        expertService.setCategory(category);
        ExpertService saveExpertService = expertServiceRepo.save(expertService);
        return expertServiceMapper.toResponse(saveExpertService);
    }


    @Override
    public Void deleteService(Long serviceId) {
        ExpertProfile  expertProfile = getCurrentExpertProfile();
        ExpertService expertService = getExpertServiceById(serviceId);
        checkAiservice(expertService,expertProfile);
        if (expertService.getServiceStatus() == ServiceStatus.DELETED) {
            throw new GlobalException(400, "Service already deleted");
        }

        // 5. Xóa mềm: đổi status sang DELETED
        expertService.setServiceStatus(ServiceStatus.DELETED);
        expertServiceRepo.save(expertService);

        return null;
    }

    //tất cả bài đăng của của 1 expert
    @Override
    public List<ExpertServiceResponse> getAllMyServiceByOpend() {
ExpertProfile expertProfile = getCurrentExpertProfile();
        return expertServiceRepo .findByExpertProfileAndServiceStatusNot(expertProfile, ServiceStatus.DELETED).stream().map(expertServiceMapper :: toResponse).toList();
    }

    @Override
    public ExpertServiceResponse getMyServiceDetail(Long serviceId) {

        ExpertProfile expertProfile = getCurrentExpertProfile();
        ExpertService expertService = getExpertServiceById(serviceId);
        checkAiservice(expertService, expertProfile);
        if (expertService.getServiceStatus() == ServiceStatus.DELETED) {
            throw new GlobalException(400, "Service already deleted");
        }

        return expertServiceMapper.toResponse(expertService);
    }

    public List<ExpertServiceResponse> getMyPurchasedServices() {
        User currentUser = currentUserService.getCurrentUser();
        ClientProfile clientProfile = clientProfileRepo.findByUser(currentUser)
                .orElseThrow(() -> new GlobalException(403, "Only client can view purchased services"));

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




    // Lấy tất cả bài đăng public của hệ thống.
    @Override
    public List<ExpertServiceResponse> getAllPublicServices() {
        return expertServiceRepo.findByServiceStatus(ServiceStatus.OPEN).stream().map(expertServiceMapper ::toResponse).toList();
    }

    // Lấy tất cả bài đăng public của hệ thống kèm bộ lọc.
    public PageResponse<ExpertServiceResponse> getAllPublicServicesPage(ExpertServiceFillerRequest expertServiceFillerRequest) {
        // Kiểm tra dữ liệu từ FE, field nào null thì dùng mặc định từ pageRequest thông qua PageUtil.
        Pageable pageable = PageUtil.createPageable(expertServiceFillerRequest);

        // Gọi DB lấy danh sách.
        // ExpertServiceSpecification.filter(...) dùng để tạo điều kiện tìm kiếm và lọc.
        // pageable dùng để phân trang và sắp xếp dữ liệu.
        Page<ExpertService> expertServicesPages =expertServiceRepo.findAll(ExpertServiceSpecification.filter(expertServiceFillerRequest),pageable)
                ;
        // Map ExpertService sang ExpertServiceResponse trong Page.
        Page<ExpertServiceResponse> responsePage = expertServicesPages.map(expertServiceMapper::toResponse);

        // Dùng PageResponse.fromPage để tự bọc content + metaData\
        // Nó lấy content trong responsePage đưa vào content.
        // Nó lấy thông tin phân trang trong responsePage đưa vào metaData.
        // Kết quả trả về là PageResponse<ExpertServiceResponse>.
        return PageResponse.fromPage(responsePage);

    }



// Chi tiết một bài đăng của hệ thống.
    @Override
    public ExpertServiceResponse getPublicServiceDetail(long serviceId) {
         ExpertService expertService = getExpertServiceById(serviceId);

        if (expertService.getServiceStatus() != ServiceStatus.OPEN) {
            throw new GlobalException(400, "Service is not public");
        }
        return expertServiceMapper.toResponse(expertService);
    }

    @Override
    public ExpertServiceResponse changeServiceStatus(Long serviceId, ServiceStatus serviceStatus) {
        ExpertProfile expertProfile = getCurrentExpertProfile();
        ExpertService expertService = getExpertServiceById(serviceId);
        checkAiservice(expertService, expertProfile);
        expertService.setServiceStatus(serviceStatus);

        ExpertService savedService = expertServiceRepo.save(expertService);
        return expertServiceMapper.toResponse(savedService);
    }


    private void validateInputContent(ExpertServiceRequest request) {
        if (request == null) {
            throw new GlobalException(400, "request is required");
        }
        contentManagerService.validateKeywordInput(request.getServiceName());
        contentManagerService.validateKeywordInput(request.getServiceDescription());

    }

    private List<Skill> getSkillBySkillId(List<Long> selectedSkillIds) {

        if (selectedSkillIds == null || selectedSkillIds.isEmpty()) {
           throw new GlobalException(400, "selectedSkillIds is required");
        }
        List<Long> checkSkillIds = new ArrayList<>();

        for (Long skillId : selectedSkillIds) {
            if (skillId == null) continue;
=======
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
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

            if (skillId <= 0) {
                continue;
            }
<<<<<<< HEAD
=======

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
            if (!checkSkillIds.contains(skillId)) {
                checkSkillIds.add(skillId);
            }
        }
<<<<<<< HEAD
        if (checkSkillIds.isEmpty()) {
            throw new GlobalException(400, "Skill is required");
        }

        List<Skill> skills = skillRepo.findAllById(checkSkillIds);

        if (skills.size() != checkSkillIds.size()) {
            throw new GlobalException(400, "Some skills not found");
=======

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
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        }

        return skills;
    }

    private Category getCategoryByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
<<<<<<< HEAD
            throw new GlobalException(400, "Category is required");
        }
        Category category = categoryRepo.getCategoriesByCategoryId(categoryId).orElseThrow(() -> new GlobalException(400, "Category Not Found"));
        return category;
    }

    private void checkAiservice(ExpertService expertService, ExpertProfile expertProfile) {
        if (!expertService.getExpertProfile().getExpertProfileId().equals(expertProfile.getExpertProfileId())) {
            throw new GlobalException(400, "expertProfileId not match");
=======
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
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        }
    }

    public ExpertProfile getCurrentExpertProfile() {
<<<<<<< HEAD
        User currentUser = currentUserService.getCurrentUser();
        return expertProfileRepo.findByUser(currentUser).orElseThrow(() -> new GlobalException(400, "User  Not Found"));
    }

    public ExpertService getExpertServiceById(Long serviceId) {
        return expertServiceRepo.findById(serviceId).orElseThrow(() -> new GlobalException(400, "expertService not found"));

    }



=======
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

    private void ensureExpertVerified(ExpertProfile expertProfile) {
        expertVerificationGuardService.ensureVerified(expertProfile);
    }
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
}
