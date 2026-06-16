package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.BlockedContentRequest;
import com.example.AiTaster.entity.BlockedContent;

import java.util.List;

public interface IBlockedContent {
    BlockedContent create(BlockedContentRequest request);
    List<BlockedContent> getAll();
    BlockedContent update(Long id, BlockedContentRequest request);
   void delete(Long id);

}
