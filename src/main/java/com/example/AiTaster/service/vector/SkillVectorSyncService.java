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
// đẩy toàn bộ skill đang có trong db lên Qdrant
public class SkillVectorSyncService {
    private final SkillRepo skillRepo;
    private final EmbeddingService embeddingService;
    private final QdrantCollectionService qdrantCollectionService;
    private final RestClient qdrantRestClient;
    private final QdrantProperties qdrantProperties;


// hàm đẩy toàn bộ skill trong db qua Qdrant
// đẩy list thì vòng lặp rồi thêm vào list rỗng bên ngoài rồi lại add rồi trả về size()
@Transactional(readOnly = true)
    public int syncSkillVector() {
        qdrantCollectionService.CheckSkillCollectionExits();

        List<Skill> skills = skillRepo.findAll();

        if(skills.isEmpty()) {
            return 0;
        }
  List<Map<String,Object>> newPoints = new ArrayList<>();
        for(Skill skill : skills) {
            Map<String, Object> point = buildQdrantPoint(skill);
            newPoints.add(point);
        }
        upsertPointToQdranr(newPoints);
        return newPoints.size();
    }


    // upseart 1 skill lên Qdrant
    // dùng khi admin tạo mới skill hoặc sửa skill
    public void upsertOneSkill(Skill skill){
        qdrantCollectionService.CheckSkillCollectionExits();
         Map<String, Object> point = buildQdrantPoint(skill);

         List<Map<String, Object>> skills = List.of(point);

    }

   // chuyển skill thành Qdrant point
   // map(string) là kiểu dữ liệu key-value giống json
   //1 skill sau khi biến thành vertor sẽ là 1 point
    private Map<String,Object> buildQdrantPoint(Skill skills){
        // b1 gom text
        String skillname = skills.getSkillName() == null ? "" :skills.getSkillName();

        String textSkill = buildSkillText(skills);

        float[] vectorSkill = embeddingService.converTextToVector(textSkill);

        return  Map.of(
                "id",skills.getSkillId(),
                "vector",vectorSkill,
                "payload",Map.of(
                        "skillId",skills.getSkillId(),
                        "skillName",skills.getSkillName()
                )

                );
    }


    // gữi danh sách point lêm Qdrant
    // List<map<String> là danh sách chứa nhiều map
    // mỗi map là 1 points
    private String upsertPointToQdranr(List<Map<String,Object>> point) {
        String  collectionName = qdrantProperties.getCollection().getSkills();
      Map<String,Object> map = Map.of("point",point); // tạo json point :
                                                                            // ......
                                                                            // ...

        qdrantRestClient.put()
                .uri("/collection/{collectionName}/points?wait=true",collectionName)
                .body(map)
                .retrieve()
                .toBodilessEntity();
        return  collectionName;
    }





// hàm này để gom thông tin để tạo thành đoạn text từ test mới thành vector
    private String buildSkillText(Skill skill) {
    String skillname = skill.getSkillName() == null ? "" : skill.getSkillName();
    String description = skill.getDescription() == null ? "" : skill.getDescription();
       return """
               Skill Name: %s
               Description: %s
              """.formatted(skillname,description);
    }

}
