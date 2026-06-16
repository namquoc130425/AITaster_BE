package com.example.AiTaster.service;

import com.example.AiTaster.constant.BlockedContentType;
import com.example.AiTaster.entity.BlockedContent;
import com.example.AiTaster.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.example.AiTaster.constant.ErrorCode.BLOCKED_KEYWORD;
import static com.example.AiTaster.constant.ErrorCode.FIELD_REQUIRED;
import static com.example.AiTaster.constant.ErrorCode.PROMPT_INJECTION;

@Service
@RequiredArgsConstructor
public class ContentManagerService {
    private final BlockedContentCacheService blockedContentCacheService;

    // Bat URL pho bien. Rule nay chay local, khong can DB/Redis.
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://|www\\.|[a-z0-9-]+\\.(com|net|org|io|vn))",
            Pattern.CASE_INSENSITIVE
    );

    // Bat so dien thoai Viet Nam dang 0... hoac +84..., cho phep chen space/dau cham/gach ngang.
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(\\+84|0)(\\d[\\s.-]?){8,10}"
    );

    // Bat email co ban.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    // Prompt injection cố định, không cần admin thêm cũng vẫn chặn.
    // Dùng Pattertn để bắt nếu người dùng viết dấu cách để lạch luật v.v.
    private static final List<Pattern> PROMPT_INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore\\s+previous\\s+instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("system\\s+prompt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("developer\\s+message", Pattern.CASE_INSENSITIVE),
            Pattern.compile("bo\\s+qua\\s+huong\\s+dan\\s+truoc", Pattern.CASE_INSENSITIVE),
            Pattern.compile("tra\\s+loi\\s+ngoai\\s+json", Pattern.CASE_INSENSITIVE)
    );

    public void validateKeywordInput(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new GlobalException(FIELD_REQUIRED.getCode(), FIELD_REQUIRED.getMessage());
        }

        String normalized = normalize(keyword); // Chuẩn hóa unicode, lowercase, trim.
        String noAccent = removeAccent(normalized); // Bỏ dấu tiếng Việt để bắt né dấu.
        String compact = compact(noAccent); // Bỏ khoảng trắng/ký tự chen giữa.
      // kiểm tra in put có chưa link hay sdt , email ko
        if (URL_PATTERN.matcher(normalized).find()
                || PHONE_PATTERN.matcher(normalized).find()
                || EMAIL_PATTERN.matcher(normalized).find()) {
            throw new GlobalException(BLOCKED_KEYWORD.getCode(), BLOCKED_KEYWORD.getMessage());
        }

        for (Pattern pattern : PROMPT_INJECTION_PATTERNS) {
            if (pattern.matcher(noAccent).find()) {
                throw new GlobalException(PROMPT_INJECTION.getCode(), PROMPT_INJECTION.getMessage());
            }
        }

        //lấy toàn bộ danh sách nội dung bị chặn,Nhưng nó không query DB trực tiếp mỗi lần,nó đi qua CacheService
        //Nếu Redis có cache → lấy từ Redis
        //Nếu Redis chưa có → query DB → lưu vào Redis → trả về
        for (BlockedContent item : blockedContentCacheService.GetAllBlockContens()) {
            if (item == null || item.getContent() == null || item.getContent().isBlank()) {
                continue;
            }

            String blocked = removeAccent(normalize(item.getContent()));
            String blockedCompact = compact(blocked);

            // noAccent.contains: bat chuoi binh thuong.
            // compact.contains: bat kieu h a c k, h-a-c-k, h@ck.
            if (noAccent.contains(blocked) || compact.contains(blockedCompact)) {
                if (item.getType() == BlockedContentType.PROMPT_INJECTION) {
                    throw new GlobalException(PROMPT_INJECTION.getCode(), PROMPT_INJECTION.getMessage());
                }

                throw new GlobalException(BLOCKED_KEYWORD.getCode(), BLOCKED_KEYWORD.getMessage());
            }
        }
    }

    private String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\p{Cntrl}]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String removeAccent(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("\u0111", "d");
    }

    private String compact(String input) {
        return input.replaceAll("[\\s._\\-@]+", "");
    }
}
