package com.example.AiTaster.service;

import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertServiceMapper;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.service.imp.IExpertService;
import lombok.RequiredArgsConstructor;
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

    @Override
    public ExpertServiceResponse CreatService(ExpertServiceRequest expertServiceRequest) {
        validateInputContent(expertServiceRequest);
        ExpertProfile expertProfile = getCurrentExpertProfile();
        Category category = getCategoryByCategoryId(expertServiceRequest.getSelectedCategoryId());
        List<Skill> skills = getSkillBySkillId(expertServiceRequest.getSelectedSkillIds());
        ExpertService newExpertService = expertServiceMapper.toEntity(expertServiceRequest, expertProfile);
        newExpertService.setSkills(skills);
        newExpertService.setCategory(category);
        expertServiceRepo.save(newExpertService);


        return expertServiceMapper.toResponse(newExpertService);

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
