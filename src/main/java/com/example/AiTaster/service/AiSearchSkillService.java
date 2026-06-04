package com.example.AiTaster.service;

import com.example.AiTaster.dto.response.Ai.AiSearchSkilResponse;
import com.example.AiTaster.dto.response.Ai.AiSkillResult;
import com.example.AiTaster.repository.SkillRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiSearchSkillService {
    private final SkillRepo skillRepo;

//tìm skill lien quan từ keywork mà Ai trả ra
// repo đã dùng mapper để xuống db querry theo keyword
// Trả về list skill từ db

    public List<AiSkillResult> searchSkillByKeyword(AiSearchSkilResponse aiSearchSkilResponse) {
       // validate dữ liệu của keywork skill oke không -> KO OKE TRẢ VỀ LIST RỖNG
   if(aiSearchSkilResponse.getKeywork() == null || aiSearchSkilResponse.getKeywork().isEmpty()){
           return List.of();
   }
    List<String> keywords = normalizeKeywords(aiSearchSkilResponse.getKeywork());

   //dùng linkedhashmap để lưu kết quả trả về từ db tránh việc trùng lặp skill ( nếu có 2 keywork trả về cùng 1 skill thì chỉ lấy 1 skill ) và giữ nguyên thứ tự tìm kiếm của keywork
        Map<Long, AiSkillResult> resultMap = new LinkedHashMap<>();
 // querry chỉ chạy đc 1 keywords 1 lần nên cần vòng lặp để querry từng key work
        for (String keyword : keywords) {
            List<AiSkillResult> skillResults = skillRepo.findBySkillNameKeyword(keyword, PageRequest.of(0, 5));
            // PageRequest(0,5) lâấy trang số 0 mỗi trang lấy tối da 5 dòng
  // duyệt lại từng dữ liệu mà db trả về
            for (AiSkillResult skillResult : skillResults) {
                resultMap.putIfAbsent(skillResult.getSkillId(), skillResult); //  putIfAbsent sẽ chỉ thêm skill vào resultMap nếu skillId chưa tồn tại trong map, nếu đã tồn tại thì sẽ bỏ qua không thêm nữa
            }
        }

        return resultMap.values()
                .stream()
                .limit(15)
                .toList();
    }






 // hàm làm sạch kết quả mà ai trá về
    private List<String> normalizeKeywords(List<String> rawKeywords) {
        Set<String> seen = new HashSet<>();

        return rawKeywords.stream()
                .filter(Objects::nonNull)  //bỏ null
                .map(String::trim)         // xóa khoảng trắng
                .filter(keyword -> !keyword.isBlank())
                .filter(keyword -> seen.add(keyword.toLowerCase(Locale.ROOT))) // bỏ trùng không phân biệt hoa thường
                .limit(5) //tôi đa 5 keywork tránh querry nhiều lần
                .toList();
    }


}
