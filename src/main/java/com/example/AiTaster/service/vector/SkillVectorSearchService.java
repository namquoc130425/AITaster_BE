package com.example.AiTaster.service.vector;

import com.example.AiTaster.config.QdrantProperties;
import com.example.AiTaster.dto.response.Ai.VectorSkillResult;
import com.example.AiTaster.service.SkillService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// service sữ lý text người dùng nhập vào
@Slf4j
@RequiredArgsConstructor
public class SkillVectorSearchService {
    private final EmbeddingService embeddingService;
    private final QdrantCollectionService qdrantCollectionService;
    private final RestClient qdrantRestClient;
    private final QdrantProperties qdrantProperties;

    // hàm này xữ lý search text mà người dùng nhập vào
public List<VectorSkillResult> searchSkillResult(String seachtext , int limit ) {
    // kiểm tra Colection trong qdrant đã tồn tại chưa
     qdrantCollectionService.CheckSkillCollectionExits();

    // chuyển text jobpost mà người dùng nhập vào thành vector để vào Qdrant tìm kím
float[] vectorQuerry = embeddingService.converTextToVector(seachtext);

    //cầm vector chuyển thành dạng json để vô  Qdrant tìm kím
    //querry : vector user nhập
    // limt : xác định trả ra bao nhiêu record
    //payload: mỗi point đi kèm với 1 payload . để true để trả về dữ liệu trong payload
    Map<String,Object> body = Map.of(
            "query",vectorQuerry,
            "limit",limit,
            "with_payload",true
    );

    // lấy tên colection trong file yaml(ko set cứng , sau này dể thay đổi thì thay đổi bên yaml)
    String collectionName = qdrantProperties.getCollection().getSkills();


    //gữi request search cho Qdrant
    //.body là gữi body là giống  point á
    //.retrive gữi và nhận
    //.body(Jsonnode.class) -> trả về Json và muốn đọc dưới dạng jsonNode
    JsonNode node = qdrantRestClient.post()
            .uri("/collections/{collectionName}/points/query", collectionName)
            .body(body)
            .retrieve()
            .body(JsonNode.class);



        // xu ly lai du lieu
    return formSkillResult(node);
}

// hàm này dùng để đọc Json từ Qdrant rồi chuyển thành list
    // hàm trên trả ra dữ liệu thô nên cần hàm này xữ lý lại dữ liệu
    private List<VectorSkillResult> formSkillResult(JsonNode response) {
    List<VectorSkillResult> results = new ArrayList<>();

        // kiểm tra hàm trên trả vè có null không -> null trả về list rỗng
        if(response == null) return results;

        // lấy danh sách points
        JsonNode pointNode = response.path("result").path("point");

        //trường hợp points trả ra forms khác thì vẫn bắt đc
        if(pointNode.isMissingNode() || !pointNode.isArray()) {
            pointNode = response.path("result");
        }

        for(JsonNode point : pointNode) {

            //lấy payload(thông tin phụ)
            JsonNode payloadNode = point.path("payload");

            // tạo biến chờ hứng dữ liệu
            Long skillId = null;
            String skillName = null;

            // lấy skillId ( ưu tiên lấy trong payload , không có thì lấy trong id )
            if(payloadNode.path("skillId").isNumber()) {
                skillId = payloadNode.path("skillId").asLong();
            }else if(payloadNode.path("id").isNumber()) {
                skillId = payloadNode.path("id").asLong();
            }
            //lấy nameSkill
            if (payloadNode.path("skillName").isTextual()) {
                skillName = payloadNode.path("skillName").asText();
            }
            // lấy score ( điểm độ chính xác ( tương đồng )) ( nếu ko có mặc định lấy 0.0 )
            double score = point.path("score").asDouble(0.0);

            if(skillId != null) {
                results.add(VectorSkillResult.builder()
                        .skillId(skillId)
                                .skillName(skillName)
                                .score(score)
                                .selectedByUser(false)
                                .build()

                        );
            }

        }

    return results;
    }





/*
ví dụ về response.path("result").path("point");
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
