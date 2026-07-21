package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.ExpertProfile;
import org.mapstruct.*;

<<<<<<< HEAD
@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ExpertProfileMapper {

=======
@Mapper(componentModel = "spring", uses = {UserMapper.class, ExpertVerificationMapper.class, CategoryMappper.class, SkillMapper.class})
public interface ExpertProfileMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "skills", ignore = true)
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    ExpertProfile registertoEntity(ExpertRegisterRequest request);

    ExpertProfileResponse toResponse(ExpertProfile expertProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
<<<<<<< HEAD
=======
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "skills", ignore = true)
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @Mapping(target = "yearOfExperience", source = "yearOfExperience")
    void updateEntity(
            ExpertProfileRequest request,
            @MappingTarget ExpertProfile profile
    );
<<<<<<< HEAD
}
=======

}
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
