package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorSkill;
import com.example.AiTaster.dto.request.SkillRequest;
import com.example.AiTaster.dto.response.SkillResponse;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.SkillMapper;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.service.imp.ISkill;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SkillService implements ISkill {
    SkillRepo skillRepo;
    SkillMapper skillMapper;

    @Override
    public SkillResponse create(SkillRequest skillRequest) {

        if (skillRepo.existsBySkillName(skillRequest.getSkillName())) {

            throw new GlobalException(ErrorSkill.SKILL_EXISTS.getCode(), ErrorSkill.SKILL_EXISTS.getMessage());
        }

        Skill skill = skillMapper.toEntity(skillRequest);
        skillRepo.save(skill);

        return skillMapper.toResponse(skill);
    }

    @Override
    public SkillResponse update(Long id, SkillRequest skillRequest) {
        Skill skill = skillRepo.findById(id).orElseThrow(() -> new GlobalException(ErrorSkill.NOT_FOUND_SKILL.getCode(), ErrorSkill.NOT_FOUND_SKILL.getMessage()));
        Skill skillUpdate = skillMapper.toUpdate(skillRequest, skill);
        skillRepo.save(skillUpdate);

        return skillMapper.toResponse((skillUpdate));
    }

    @Override
    public SkillResponse getById(Long id) {
        Skill skill = skillRepo.findById(id).orElseThrow(
                () -> new GlobalException(ErrorSkill.NOT_FOUND_SKILL.getCode()
                        , ErrorSkill.NOT_FOUND_SKILL.getMessage()));
        return skillMapper.toResponse(skill);
    }

    @Override
    public List<SkillResponse> getAll() {

        List<Skill> skills = skillRepo.findAll();

        return skillMapper.toReponseList(skills);
    }

    @Override
    public SkillResponse delete(Long id) {
        Skill skill = skillRepo.findById(id).orElseThrow(
                () -> new GlobalException(ErrorSkill.NOT_FOUND_SKILL.getCode()
                        , ErrorSkill.NOT_FOUND_SKILL.getMessage()));
        skillRepo.delete(skill);
        return null;
    }
}
