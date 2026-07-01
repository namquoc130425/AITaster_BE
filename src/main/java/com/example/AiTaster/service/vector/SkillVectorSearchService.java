package com.example.AiTaster.service.vector;

import com.example.AiTaster.config.QdrantProperties;
import com.example.AiTaster.dto.response.Ai.VectorSkillResult;
import com.example.AiTaster.service.SkillService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Service xử lý text người dùng nhập vào.
@Service
@Slf4j
@RequiredArgsConstructor
public class SkillVectorSearchService {
    private final EmbeddingService embeddingService;
    private final QdrantCollectionService qdrantCollectionService;
    private final RestClient qdrantRestClient;
    private final QdrantProperties qdrantProperties;

    // Hàm này xử lý search text mà người dùng nhập vào.
public List<VectorSkillResult> searchSkillResult(String seachtext , int limit ) {
    // Kiểm tra collection trong Qdrant đã tồn tại chưa.
     qdrantCollectionService.checkSkillCollectionExits();

    // Chuyển text job post người dùng nhập thành vector để tìm kiếm trong Qdrant.
float[] vectorQuerry = embeddingService.converTextToVector(seachtext);

    // Chuyển vector thành JSON để tìm kiếm trong Qdrant.
    // query: vector user nhập.
    // limit: xác định trả ra bao nhiêu record.
    // with_payload=true để trả về dữ liệu payload đi kèm mỗi point.
    Map<String,Object> body = Map.of(
            "query",vectorQuerry,
            "limit",limit,
            "with_payload",true
    );

    // Lấy tên collection trong file YAML, không set cứng để dễ thay đổi.
    String collectionName = qdrantProperties.getCollection().getSkills();


    // Gửi request search cho Qdrant.
    // .body gửi body request.
    // .retrieve gửi request và nhận response.
    // .body(JsonNode.class) đọc response dưới dạng JsonNode.
    JsonNode node = qdrantRestClient.post()
            .uri("/collections/{collectionName}/points/query", collectionName)
            .body(body)
            .retrieve()
            .body(JsonNode.class);



        // Xử lý lại dữ liệu.
    return formSkillResult(node);
}

// Hàm này đọc JSON từ Qdrant rồi chuyển thành list.
    // Hàm trên trả dữ liệu thô nên cần hàm này xử lý lại dữ liệu.
    private List<VectorSkillResult> formSkillResult(JsonNode response) {
    List<VectorSkillResult> results = new ArrayList<>();

        // Nếu response null thì trả về list rỗng.
        if(response == null) return results;

        // Lấy danh sách points.
        JsonNode pointNode = response.path("result").path("points");

        // Trường hợp points trả ra format khác thì vẫn bắt được.
        if(pointNode.isMissingNode() || !pointNode.isArray()) {
            pointNode = response.path("result");
        }

        for(JsonNode point : pointNode) {

            // Lấy payload là thông tin phụ.
            JsonNode payloadNode = point.path("payload");

            // Tạo biến để hứng dữ liệu.
            Long skillId = null;
            String skillName = null;

            // Lấy skillId, ưu tiên lấy trong payload; nếu không có thì lấy trong id.
            if(payloadNode.path("skillId").isNumber()) {
                skillId = payloadNode.path("skillId").asLong();
            }else if(point.path("id").isNumber()) {
                skillId = point.path("id").asLong();
            }
            // Lấy skillName.
            if (payloadNode.path("skillName").isTextual()) {
                skillName = payloadNode.path("skillName").asText();
            }
            // Lấy score là điểm tương đồng; nếu không có thì mặc định 0.0.
            double score = point.path("score").asDouble(0.0);

            if(skillId != null) {
                results.add(VectorSkillResult.builder()
                        .skillId(skillId)
                                .skillName(skillName)
                                .score(score)
                                .build()

                        );
            }

        }

    return results;
    }





/*
Ví dụ về response.path("result").path("point"):
{
  "result": {
    "points": [
      {
        "id": 1,
        "score": 0.89,
        "payload": {
          "skillId": 1,
          "skillName": "React"
        }
      }
    ]
  }
}

 */





}
