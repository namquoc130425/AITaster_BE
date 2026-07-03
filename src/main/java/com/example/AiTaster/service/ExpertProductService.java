package com.example.AiTaster.service;

import com.example.AiTaster.Util.PageUtil;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.ProductType;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.dto.response.InvoiceResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertServiceMapper;
import com.example.AiTaster.repository.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final ServiceFileRepo serviceFileRepo;
    private final LocalFileStorageService localFileStorageService;
    private final ClientProfileRepo clientProfileRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;

    public record ServiceFileDownload(
            Resource resource,
            String fileName,
            String contentType,
            long contentLength
    ) {}

    @Override
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

        String docUrl =
                localFileStorageService.saveFile(
                        request.getDocFile()
                );

        String sourceUrl =
                localFileStorageService.saveFile(
                        request.getSourceFile()
                );

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
                .map(paymentTransaction ->
                        expertServiceRepo.findById(paymentTransaction.getReferenceId())
                                .map(expertService -> toPurchasedServiceResponse(expertService, paymentTransaction))
                                .orElse(null)
                )
                .filter(Objects::nonNull)
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
        if (serviceStatus == ServiceStatus.DELETED) {
            throw new GlobalException(400, "Use delete endpoint to delete service");
        }
        if (expertService.getServiceStatus() == ServiceStatus.DELETED) {
            throw new GlobalException(400, "Service already deleted");
        }
        expertService.setServiceStatus(serviceStatus);

        ExpertService savedService = expertServiceRepo.save(expertService);
        return expertServiceMapper.toResponse(savedService);
    }

    @Transactional(readOnly = true)
    public ServiceFileDownload downloadServiceFile(Long serviceId, Long serviceFileId, String kind) {
        ExpertService expertService = getExpertServiceById(serviceId);
        ServiceFile serviceFile = expertService.getServiceFile();

        if (serviceFile == null || !serviceFileId.equals(serviceFile.getServiceFileId())) {
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

        Path filePath = resolveLocalUploadPath(filePathValue);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new GlobalException(404, "Service file not found on server");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new GlobalException(404, "Service file is not readable");
            }

            String contentType = Files.probeContentType(filePath);
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
            throw new GlobalException(400, "request is required");
        }
        contentManagerService.validateKeywordInput(request.getServiceName());
        contentManagerService.validateKeywordInput(request.getServiceDescription());

    }

    private void checkCanDownloadServiceFile(ExpertService expertService) {
        User currentUser = currentUserService.getCurrentUser();
        boolean isExpertOwner = expertProfileRepo.findByUser(currentUser)
                .map(expertProfile -> expertService.getExpertProfile() != null
                        && expertProfile.getExpertProfileId().equals(
                        expertService.getExpertProfile().getExpertProfileId()
                ))
                .orElse(false);
        boolean isPurchasedClient = paymentTransactionRepo
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
        ExpertServiceResponse response = expertServiceMapper.toResponse(expertService);
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

        String relativePath = filePathValue.replace('\\', '/');
        while (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        Path uploadsRoot = Path.of("uploads").toAbsolutePath().normalize();
        Path filePath = Path.of(relativePath).toAbsolutePath().normalize();

        if (!filePath.startsWith(uploadsRoot)) {
            throw new GlobalException(403, "Invalid service file path");
        }

        return filePath;
    }

    private String resolveDownloadFileName(String filePathValue) {
        String fileName = filePathValue.replace('\\', '/');
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        int separatorIndex = fileName.indexOf("_");
        if (separatorIndex >= 0 && separatorIndex + 1 < fileName.length()) {
            return fileName.substring(separatorIndex + 1);
        }

        return fileName.isBlank() ? "service-file" : fileName;
    }

    private List<Skill> getSkillBySkillId(List<Long> selectedSkillIds) {

        if (selectedSkillIds == null || selectedSkillIds.isEmpty()) {
           throw new GlobalException(400, "selectedSkillIds is required");
        }
        List<Long> checkSkillIds = new ArrayList<>();

        for (Long skillId : selectedSkillIds) {
            if (skillId == null) continue;

            if (skillId <= 0) {
                continue;
            }
            if (!checkSkillIds.contains(skillId)) {
                checkSkillIds.add(skillId);
            }
        }
        if (checkSkillIds.isEmpty()) {
            throw new GlobalException(400, "Skill is required");
        }

        List<Skill> skills = skillRepo.findAllById(checkSkillIds);

        if (skills.size() != checkSkillIds.size()) {
            throw new GlobalException(400, "Some skills not found");
        }

        return skills;
    }

    private Category getCategoryByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new GlobalException(400, "Category is required");
        }
        Category category = categoryRepo.getCategoriesByCategoryId(categoryId).orElseThrow(() -> new GlobalException(400, "Category Not Found"));
        return category;
    }

    private void checkAiservice(ExpertService expertService, ExpertProfile expertProfile) {
        if (!expertService.getExpertProfile().getExpertProfileId().equals(expertProfile.getExpertProfileId())) {
            throw new GlobalException(400, "expertProfileId not match");
        }
    }

    public ExpertProfile getCurrentExpertProfile() {
        User currentUser = currentUserService.getCurrentUser();
        return expertProfileRepo.findByUser(currentUser).orElseThrow(() -> new GlobalException(400, "User  Not Found"));
    }

    public ExpertService getExpertServiceById(Long serviceId) {
        return expertServiceRepo.findById(serviceId).orElseThrow(() -> new GlobalException(400, "expertService not found"));

    }



}
