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
// hàm này để lấy danh sách nội dụng bị chặn dưới db để lưu vào redis cache để lần sau kiểm tra nhanh hơn thay vì querry liên tục
public class BlockedContentCacheService {

    private final BlockedContentRepo blockedContentRepo;

    @Cacheable(value = "BlockedContent", key = "'all'")
    // anotation này giống như vòng lặp BlockedContent:: all để kiểm tra trong đó có dữ liệu ko nếu ko có thì xuống db lấy dữ liệu truyền vào redis cache , cache sẽ hết hiệu lực trong vòng 30P . có thì lấy trong cache , ko có xuong db truyền vào trong cache và lần sau sẽ lấy trong cache thay vì xuống db lấy.blockedContents::all = [hack, scam]
    public List<BlockedContent> GetAllBlockContens() {
        return blockedContentRepo.findAll();
    }

    @CacheEvict(value = "BlockedContent", allEntries = true)
    // Xóa cache sau khi admin create/update/delete blacklist.
    // allEntries=true để clear toàn bộ cache group này.
    public void clearBlockedContentCache() {
        // Method rỗng là bình thường, mục đích chỉ để trigger @CacheEvict.
    }

}
