package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.request.ResubmitExpertCertificateRequest;
import com.example.AiTaster.dto.response.CurrentUserResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.dto.response.ExpertVerificationResponse;
import com.example.AiTaster.dto.response.PublicExpertProfileResponse;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertVerification;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.mapper.ExpertProfileMapper;
import com.example.AiTaster.mapper.ExpertVerificationMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.ExpertVerificationRepo;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.repository.RatingRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IExpertProfile;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class ExpertProfileService implements IExpertProfile {
@Autowired
    ExpertProfileMapper expertProfileMapper;
@Autowired
    ExpertProfileRepo expertProfileRepo;
@Autowired
    UserRepo userRepo;
@Autowired
    CurrentUserResponseMapper currentUserResponseMapper;

@Autowired
    UserMapper userMapper;
@Autowired
    CurrentUserService currentUserService;
@Autowired
    ExpertVerificationRepo expertVerificationRepo;
@Autowired
    ExpertVerificationMapper expertVerificationMapper;
@Autowired
    NotificationService notificationService;
@Autowired
    ExpertServiceRepo expertServiceRepo;
@Autowired
    CategoryRepo categoryRepo;
@Autowired
    SkillRepo skillRepo;
@Autowired
    RatingRepo ratingRepo;
@Autowired
    ProjectRepo projectRepo;


    @Override
    public List<ExpertProfileResponse> getAll() {
        return expertProfileRepo
                .findAll().
                stream().
                map(this::toResponseWithStatistics)
                .toList();
    }

@Override
    public ExpertProfileResponse getByExpertId(Long expertId) {
        ExpertProfile profile = expertProfileRepo.findById(expertId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(), "Hồ sơ chuyên gia: " + ErrorCode.NOT_FOUND.getMessage()));
        return toResponseWithStatistics(profile);
    }

    @Override
    public ExpertProfileResponse getByUserId(Long userId) {
        ExpertProfile profile = expertProfileRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(), "Hồ sơ chuyên gia: " + ErrorCode.NOT_FOUND.getMessage()));
        return toResponseWithStatistics(profile);
    }

    @Transactional
    public PublicExpertProfileResponse getPublicProfile(Long expertProfileId) {
        ExpertProfile profile = expertProfileRepo.findByExpertProfileId(expertProfileId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy hồ sơ chuyên gia"));

        ExpertVerificationStatus verificationStatus = profile.getVerification() != null
                ? profile.getVerification().getVerificationStatus()
                : null;

        if (!ExpertVerificationStatus.VERIFIED.equals(verificationStatus)) {
            throw new GlobalException(404, "Không tìm thấy hồ sơ chuyên gia");
        }

        ExpertProfileResponse profileResponse = toResponseWithStatistics(profile);
        User user = profile.getUser();

        return PublicExpertProfileResponse.builder()
                .expertProfileId(profile.getExpertProfileId())
                .expertUserId(user != null ? user.getUserId() : null)
                .expertName(user != null ? user.getFullName() : null)
                .username(user != null ? user.getUsername() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .bio(profile.getBio())
                .category(profileResponse.getCategory())
                .skills(profileResponse.getSkills())
                .yearOfExperience(profile.getYearOfExperience())
                .portfolioUrl(profile.getPortfolioUrl())
                .rating(profileResponse.getRating())
                .ratingCount(profileResponse.getRatingCount())
                .completedProjects(profileResponse.getCompletedProjects())
                .openAiServiceCount(expertServiceRepo.countByExpertProfile_ExpertProfileIdAndServiceStatus(
                        profile.getExpertProfileId(),
                        ServiceStatus.OPEN
                ))
                .verificationStatus(verificationStatus)
                .createAt(profile.getCreateAt())
                .updateAt(profile.getUpdateAt())
                .build();
    }

    @Override
    public ExpertProfileResponse createForRegister(User user, ExpertRegisterRequest request) {
        // kiểm tra tồn tại ko
        if(expertProfileRepo.existsByUser_UserId(user.getUserId())) {
            throw new GlobalException("Người dùng này đã có hồ sơ khách hàng");
        }

        // Mapper chuyển dữ liệu yêu cầu sang entity.
          ExpertProfile expertProfile = expertProfileMapper.registertoEntity(request);
        //  Gắn User vừa tạo vào ExpertProfile
        expertProfile.setUser(user);

        // Lưu database.
        ExpertProfile save = expertProfileRepo.save(expertProfile);
        return expertProfileMapper.toResponse(save);
    }

    @Override
    @Transactional
    public CurrentUserResponse update(Long id, ExpertProfileRequest request) {
        ExpertProfile profile = expertProfileRepo.findByExpertProfileId(id).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(), "Hồ sơ chuyên gia: " + ErrorCode.NOT_FOUND.getMessage()));
        ExpertProfile currentProfile = getCurrentExpertProfile();

        if (!profile.getExpertProfileId().equals(currentProfile.getExpertProfileId())) {
            throw new GlobalException(403, "Bạn không sở hữu hồ sơ chuyên gia này");
        }

        User user = profile.getUser();

        validateUniqueAccountInformation(user, request);

        Category category = categoryRepo.getCategoriesByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy danh mục"));
        List<Long> distinctSkillIds = request.getSkillIds().stream().distinct().toList();
        List<Skill> skills = skillRepo.findAllById(distinctSkillIds);

        if (skills.size() != distinctSkillIds.size()) {
            throw new GlobalException(404, "Có kỹ năng không tồn tại");
        }

        expertProfileMapper.updateEntity(request,profile);
        profile.setCategory(category);
        profile.setSkills(skills);
        userMapper.updateUserFromExpertProfileRequest(request, user);
        ExpertProfile updateProfile = expertProfileRepo.save(profile);

        populateStatistics(updateProfile);

        return currentUserResponseMapper.toResponse(updateProfile.getUser());
    }

    // Hàm nộp lại chứng chỉ Expert khi hồ sơ bị admin từ chối.
    @Transactional
    public ExpertVerificationResponse resubmitCertificate(ResubmitExpertCertificateRequest request) {
        ExpertProfile expertProfile = getCurrentExpertProfile();
        ExpertVerification verification = expertVerificationRepo.findByExpertProfile(expertProfile)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy yêu cầu xác minh"));

        if (verification.getVerificationStatus() == ExpertVerificationStatus.VERIFIED) {
            throw new GlobalException(400, "Chuyên gia đã được xác minh không cần gửi lại chứng chỉ");
        }

        verification.setCertificateUrl(request.getCertificateUrl());
        verification.setVerificationStatus(ExpertVerificationStatus.SUBMITTED);
        verification.setRejectReason(null);
        verification.setVerifiedAt(null);
        verification.setVerifiedByAdminId(null);

        ExpertVerification saved = expertVerificationRepo.save(verification);

        userRepo.findByRole(Role.ADMIN).forEach(admin ->
                notificationService.notify(
                        admin,
                        NotificationType.SYSTEM,
                        ReferenceType.NONE,
                        saved.getVerificationId(),
                        "Có chứng chỉ chuyên gia chờ duyệt",
                        "Một chứng chỉ chuyên gia đang chờ được xét duyệt."
                )
        );

        return expertVerificationMapper.toResponse(saved);
    }


    @Transactional
    @Override
    public void  delete(Long id) {
        ExpertProfile profile = expertProfileRepo.findById(id)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(), "Hồ sơ chuyên gia: " + ErrorCode.NOT_FOUND.getMessage()));

        // muốn xóa profie của tk nào đó thì phải cắt quan hệ của User --- Profile . còn muốn xóa User mà đi kèm profile thì qua user làm
        User user = profile.getUser();

        if (user != null) {
            user.setExpertProfile(null);
        }
            profile.setUser(null);
        expertProfileRepo.delete(profile);
    }

    private ExpertProfile getCurrentExpertProfile() {
        User user = currentUserService.getCurrentUser();
        return expertProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Chỉ chuyên gia mới có thể thực hiện thao tác này"));
    }

    private ExpertProfileResponse toResponseWithStatistics(ExpertProfile profile) {
        populateStatistics(profile);
        return expertProfileMapper.toResponse(profile);
    }

    private void populateStatistics(ExpertProfile profile) {
        if (profile == null || profile.getExpertProfileId() == null) {
            return;
        }

        Long profileId = profile.getExpertProfileId();
        long ratingCount = ratingRepo.countByExpertProfile_ExpertProfileId(profileId);
        Double ratingAverage = ratingRepo.averageByExpertProfileId(profileId);
        long completedProjects = projectRepo
                .countByInvitation_ExpertApplication_ExpertProfile_ExpertProfileIdAndProjectStatus(
                        profileId,
                        ProjectStatus.COMPLETED
                );

        profile.setRating(ratingAverage == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(ratingAverage).setScale(2, RoundingMode.HALF_UP));
        profile.setRatingCount(Math.toIntExact(ratingCount));
        profile.setCompletedProjects(Math.toIntExact(completedProjects));
    }

    private void validateUniqueAccountInformation(User user, ExpertProfileRequest request) {
        Long userId = user.getUserId();

        if (userRepo.existsByEmailAndUserIdNot(request.getEmail(), userId)) {
            throw new GlobalException(409, "Email đã được sử dụng");
        }
        if (userRepo.existsByPhoneAndUserIdNot(request.getPhone(), userId)) {
            throw new GlobalException(409, "Số điện thoại đã được sử dụng");
        }
        if (userRepo.existsByUsernameAndUserIdNot(request.getUsername(), userId)) {
            throw new GlobalException(409, "Tên người dùng đã được sử dụng");
        }
    }
}
