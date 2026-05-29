package com.example.AiTaster.service.skillsSevice;

import com.example.AiTaster.constant.ErrorSkill;
import com.example.AiTaster.dto.request.skillsRequest.SkillRequest;
import com.example.AiTaster.dto.response.skillsResponse.SkillResponse;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.SkillMapper;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.service.imp.ISkill;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Builder
public class SkillService implements ISkill {

    SkillRepo skillRepo;
    SkillMapper skillMapper;

    @Override
    public SkillResponse create(SkillRequest skillRequest) {

        if (skillRepo.existsBySkillName(skillRequest.getSkillName())) {

            throw new GlobalException(ErrorSkill.SKILL_EXISTS.getMessage());

        }

        Skill skill = skillMapper.toEntity(skillRequest);

        skillRepo.save(skill);


        return skillMapper.toResponse(skill);
    }

    @Override
    public List<SkillResponse> getAll() {
        return skillRepo.findAll()
                .stream()
                .map(skillMapper::toResponse)
                .toList();
    }



    @Override
    public SkillResponse getSkillById(long id) {

        Skill skill = skillRepo.findById(id).orElseThrow(
                () -> new GlobalException(ErrorSkill.NOT_FOUND_SKILL.getMessage()));

        return skillMapper.toResponse(skill);
    }


    @Override
    public SkillResponse updateEnity(SkillRequest skillRequest, long id) {
        Skill skillEntity = skillRepo.findById(id).orElseThrow(
                () -> new GlobalException(ErrorSkill.NOT_FOUND_SKILL.getMessage())

        );

        Skill skillUpdate = skillMapper.updateEnity(skillRequest, skillEntity);
        skillRepo.save(skillUpdate);

        return skillMapper.toResponse(skillUpdate);
    }


    @Override
    public SkillResponse deleteSkillById(long id) {

        Skill skill = skillRepo.findById(id).orElseThrow(
                () -> new GlobalException(ErrorSkill.NOT_FOUND_SKILL.getMessage()));
        skillRepo.deleteById(id);
        return null;
    }
}
