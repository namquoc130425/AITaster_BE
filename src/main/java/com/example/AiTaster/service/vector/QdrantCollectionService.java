package com.example.AiTaster.service.vector;

import com.example.AiTaster.config.QdrantProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
@RequiredArgsConstructor
// hàm để kiểm tra và tạo Colellection nếu chưa có
// thêm 1 hàm để tự động xuống Db để lấy dữ liệu mới nhất để tạo colection đó là service:SkillVectorSysService
public class QdrantCollectionService {
    private final RestClient qdrantRestClient;
    private final QdrantProperties qdrantProperties;

    public void checkSkillCollectionExits() {

        String collectionName = qdrantProperties.getCollection().getSkills();      // lấy ten collection từ file yml

        // tạo API để check
        try{
         qdrantRestClient.get()
                 .uri("/collections/{collectionName}",collectionName)
                 .retrieve()  // gữi request đi rồi nhận response
                 .toBodilessEntity(); // request có thành công hay không,không quan tâm đến body trả về, chỉ cần biết status code



        }catch (RestClientResponseException exception) {

            if(exception.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404)) ) {
                createCollection(collectionName);
                return;
            }
            throw exception; // lỗi khác 404 thì báo ra

        }
    }

    private void createCollection(String collectionName) {
        //Qdrant dùng json nên dùng Map để tạo form build JSON
        Map<String ,Object> body = Map.of(
                "vectors",Map.of(
                            "size",qdrantProperties.getVectorSize(),
                            "distance",qdrantProperties.getDistance()
                )
        );

        // tạo api để thêm colection

        qdrantRestClient.put()
                .uri("/collections/{collectionName}",collectionName)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        System.out.println("Collection " + collectionName + " created successfully.");
    }

}
