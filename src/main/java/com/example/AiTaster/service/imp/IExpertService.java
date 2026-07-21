package com.example.AiTaster.service.imp;

<<<<<<< HEAD
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.ServiceFile;
=======
import com.example.AiTaster.dto.request.ExpertServiceRejectRequest;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

import java.util.List;

public interface IExpertService {
<<<<<<< HEAD
     ExpertServiceResponse CreatService(ExpertServiceRequest expertServiceRequest);


    ExpertServiceResponse updateService(Long serviceId, ExpertServiceRequest expertServiceRequest);

=======

    ExpertServiceResponse CreatService(ExpertServiceRequest expertServiceRequest);

    ExpertServiceResponse updateService(
            Long serviceId,
            ExpertServiceRequest expertServiceRequest
    );
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

    Void deleteService(Long serviceId);

    List<ExpertServiceResponse> getAllMyServiceByOpend();

    List<ExpertServiceResponse> getAllPublicServices();

<<<<<<< HEAD
    ExpertServiceResponse  getPublicServiceDetail(long serviceId);

    // Đổi trạng thái service.
    ExpertServiceResponse changeServiceStatus(Long serviceId, ServiceStatus serviceStatus);
    ExpertServiceResponse getMyServiceDetail(Long serviceId);




}
=======
    ExpertServiceResponse getPublicServiceDetail(long serviceId);

    ExpertServiceResponse getMyServiceDetail(Long serviceId);

    ExpertServiceResponse resubmitRejectedService(Long serviceId);

    ExpertServiceResponse acceptService(Long serviceId);

    ExpertServiceResponse rejectService(
            Long serviceId,
            ExpertServiceRejectRequest request
    );

    List<ExpertServiceResponse> getReviewQueueServices();

    List<ExpertServiceResponse> getDraftServices();
}
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
