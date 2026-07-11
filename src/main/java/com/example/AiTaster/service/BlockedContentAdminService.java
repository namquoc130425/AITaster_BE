package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.BlockedContentRequest;
import com.example.AiTaster.entity.BlockedContent;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.BlockedContentMapper;
import com.example.AiTaster.repository.BlockedContentRepo;
import com.example.AiTaster.service.imp.IBlockedContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockedContentAdminService implements IBlockedContent {
    private final BlockedContentRepo blockedContentRepo;
    private final BlockedContentCacheService cacheService;
    private final BlockedContentMapper blockedContentMapper;

    @Override
    public BlockedContent create(BlockedContentRequest request) {
        String content = request.getContent().trim();

        if (blockedContentRepo.existsByContentIgnoreCase(content)) {
            throw new GlobalException(400, "Nội dung bị chặn đã tồn tại");
        }

        BlockedContent blockedContent = blockedContentMapper.toEntity(request);
        blockedContent.setContent(content);
        BlockedContent saveBlockContent = blockedContentRepo.save(blockedContent);
        cacheService.clearBlockedContentCache();
        return saveBlockContent;
    }

    public List<BlockedContent> getAll() {
        return blockedContentRepo.findAll();
    }

    @Override
    public BlockedContent update(Long id, BlockedContentRequest request) {
        BlockedContent blockedContent  = blockedContentRepo.findById(id)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy nội dung bị chặn"));

        String content = request.getContent().trim();

        if (blockedContentRepo.existsByContentIgnoreCaseAndBlockedContentIdNot(content, id)) {
            throw new GlobalException(400, "Nội dung bị chặn đã tồn tại");
        }
        blockedContentMapper.updateEntity(request,blockedContent);
        blockedContent.setContent(content);
       BlockedContent blockedContent1 = blockedContentRepo.save(blockedContent);
        cacheService.clearBlockedContentCache();
        return blockedContent1;
    }

    @Override
    public void delete(Long id) {
        BlockedContent entity = blockedContentRepo.findById(id)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy nội dung bị chặn"));


        blockedContentRepo.delete(entity);
        cacheService.clearBlockedContentCache();

    }


}
