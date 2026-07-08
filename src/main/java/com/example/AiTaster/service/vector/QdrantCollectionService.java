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
// Hàm kiểm tra và tạo collection nếu chưa có.
//Collection trong qdran là đơn vị logic chưa vectors + Payloads. Mỗi collection có thể có nhiều point, mỗi point có vector + payload.
//Collection tương đương với table trong SQL . đảm bảo có nơi để lưu dữ liệu slill vector dã tồn tại trước khi mình insert dữ liệu vào
// Có thể thêm service tự lấy dữ liệu mới nhất từ DB để tạo collection, ví dụ SkillVectorSyncService.
public class QdrantCollectionService {
    private final RestClient qdrantRestClient;
    private final QdrantProperties qdrantProperties;

    public void checkSkillCollectionExits() {

        String collectionName = qdrantProperties.getCollection().getSkills();      // Lấy tên collection từ file YAML.

        // Gọi API để kiểm tra collection.
        try{
         qdrantRestClient.get()
                 .uri("/collections/{collectionName}",collectionName)
                 .retrieve()  // Gửi request đi rồi nhận response.
                 .toBodilessEntity(); // Chỉ cần biết status code, không cần body trả về.



        }catch (RestClientResponseException exception) {

            if(exception.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404)) ) {
                createCollection(collectionName);
                return;
            }
            throw exception; // Lỗi khác 404 thì báo ra.

        }
    }

    private void createCollection(String collectionName) {
        // Qdrant dùng JSON nên dùng Map để build body.
        Map<String ,Object> body = Map.of(
                "vectors",Map.of(
                            "size",qdrantProperties.getVectorSize(),
                            "distance",qdrantProperties.getDistance()
                )
        );

        // Gọi API để tạo collection.

        qdrantRestClient.put()
                .uri("/collections/{collectionName}",collectionName)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        System.out.println("Collection " + collectionName + " created successfully.");
    }

}
