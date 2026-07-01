package com.example.AiTaster.service.vector;

import com.example.AiTaster.config.QdrantProperties;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.repository.SkillRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
// Đẩy toàn bộ skill đang có trong DB lên Qdrant.
public class SkillVectorSyncService {
    private final SkillRepo skillRepo;
    private final EmbeddingService embeddingService;
    private final QdrantCollectionService qdrantCollectionService;
    private final RestClient qdrantRestClient;
    private final QdrantProperties qdrantProperties;


// Hàm đẩy toàn bộ skill trong DB qua Qdrant.
// Duyệt danh sách skill, build point rồi trả về số point đã đẩy.
@Transactional(readOnly = true)
    public int syncSkillVector() {
        qdrantCollectionService.checkSkillCollectionExits();

        List<Skill> skills = skillRepo.findAll();

        if(skills.isEmpty()) {
            return 0;
        }
  List<Map<String,Object>> newPoints = new ArrayList<>();
        for(Skill skill : skills) {
            Map<String, Object> points = buildQdrantPoint(skill);
            newPoints.add(points);
        }
        upsertPointsToQdranr(newPoints);
        return newPoints.size();
    }


    // Upsert một skill lên Qdrant.
    // Dùng khi admin tạo mới hoặc sửa skill.
    public void upsertOneSkill(Skill skill){
        qdrantCollectionService.checkSkillCollectionExits();
         Map<String, Object> points = buildQdrantPoint(skill);

         List<Map<String, Object>> skills = List.of(points);

         upsertPointsToQdranr(skills);

    }

   // Chuyển skill thành Qdrant point.
   // Map là kiểu dữ liệu key-value giống JSON.
   // Một skill sau khi biến thành vector sẽ là một point.
    private Map<String,Object> buildQdrantPoint(Skill skills){
        // Bước 1: gom text.
        String skillname = skills.getSkillName() == null ? "" :skills.getSkillName();

        String textSkill = buildSkillText(skills);

        float[] vectorSkill = embeddingService.converTextToVector(textSkill);

        return  Map.of(
                "id",skills.getSkillId(),
                "vector",vectorSkill,
                "payload",Map.of(
                        "skillId",skills.getSkillId(),
                        "skillName",skillname
                )

                );
    }


    // Gửi danh sách point lên Qdrant.
    // List<Map<String, Object>> là danh sách chứa nhiều map.
    // Mỗi map là một point.
    private String upsertPointsToQdranr(List<Map<String,Object>> points) {
        String  collectionName = qdrantProperties.getCollection().getSkills();
      Map<String,Object> map = Map.of("points",points); // Tạo JSON point.
                                                                            // ......
                                                                            // ...

        qdrantRestClient.put()
                .uri("/collections/{collectionName}/points?wait=true",collectionName)
                .body(map)
                .retrieve()
                .toBodilessEntity();
        return  collectionName;
    }





// Hàm này gom thông tin thành đoạn text rồi mới chuyển thành vector.
    private String buildSkillText(Skill skill) {
    String skillname = skill.getSkillName() == null ? "" : skill.getSkillName();
    String description = skill.getDescription() == null ? "" : skill.getDescription();
       return """
               Skill Name: %s
               Description: %s
              """.formatted(skillname,description);
    }

}
