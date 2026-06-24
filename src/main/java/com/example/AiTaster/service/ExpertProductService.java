package com.example.AiTaster.service;

import com.example.AiTaster.Util.PageUtil;
import com.example.AiTaster.constant.ProductType;
import com.example.AiTaster.constant.ServiceStatus;
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




    //tất cả bài đăng của hệ thống
    @Override
    public List<ExpertServiceResponse> getAllPublicServices() {
        return expertServiceRepo.findByServiceStatus(ServiceStatus.OPEN).stream().map(expertServiceMapper ::toResponse).toList();
    }

    //tất cả bài đăng của hệ thống kèm filter
    public PageResponse<ExpertServiceResponse> getAllPublicServicesPage(ExpertServiceFillerRequest expertServiceFillerRequest) {
        //kiểm tra dữ liệu của fe nếu cái nào null thì dùng dữ liệu của pageRequest thông qua PageUtil
        Pageable pageable = PageUtil.createPageable(expertServiceFillerRequest);

        //gọi db lấy dánh sách
        // ExpertServiceSpecification.filter(...) dùng để tạo điều kiện search/filter.
        // pageable dùng để phân trang và sắp xếp dữ liệu.
        Page<ExpertService> expertServicesPages =expertServiceRepo.findAll(ExpertServiceSpecification.filter(expertServiceFillerRequest),pageable)
                ;
        // mapp expertService sang expertResponse ở ngoài là Page những trong dữ liệu đã map
        Page<ExpertServiceResponse> responsePage = expertServicesPages.map(expertServiceMapper::toResponse);

        // Dùng PageResponse.fromPage để tự bọc content + metaData\
        // Nó lấy content trong responsePage đưa vào content.
        // Nó lấy thông tin phân trang trong responsePage đưa vào metaData.
        // Kết quả trả về là PageResponse<ExpertServiceResponse>.
        return PageResponse.fromPage(responsePage);

    }



// chi tiết 1 bài đăng cua he thong
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
