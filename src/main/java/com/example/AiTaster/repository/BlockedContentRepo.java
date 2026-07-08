package com.example.AiTaster.repository;

import com.example.AiTaster.entity.BlockedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BlockedContentRepo extends JpaRepository<BlockedContent, Long> {
    // Dùng khi create để chống nhập trùng blacklist.
    boolean existsByContentIgnoreCase(String content);

    // Dùng khi update để chống trùng với record khác.
    boolean existsByContentIgnoreCaseAndBlockedContentIdNot(String content, Long blockedContentId
    );

    List<BlockedContent> findAll();

}