package com.example.AiTaster.service;

import com.example.AiTaster.entity.BlockedContent;
import com.example.AiTaster.repository.BlockedContentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor()
// Lấy danh sách nội dung bị chặn từ DB và lưu vào Redis cache để kiểm tra nhanh hơn.
public class BlockedContentCacheService {

    private final BlockedContentRepo blockedContentRepo;

    @Cacheable(value = "BlockedContent", key = "'all'")
    // @Cacheable cache danh sách BlockedContent::all; nếu cache chưa có thì lấy từ DB rồi lưu lại.
    public List<BlockedContent> GetAllBlockContens() {
        return blockedContentRepo.findAll();
    }

    @CacheEvict(value = "BlockedContent", allEntries = true)
    // Xóa cache sau khi admin tạo, cập nhật hoặc xóa blacklist.
    // allEntries=true để xóa toàn bộ nhóm cache này.
    public void clearBlockedContentCache() {
        // Hàm rỗng là bình thường, mục đích chỉ để kích hoạt @CacheEvict.
    }

}
