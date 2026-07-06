package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.ExpertServiceRejectRequest;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;

import java.util.List;

public interface IExpertService {

    ExpertServiceResponse CreatService(ExpertServiceRequest expertServiceRequest);

    ExpertServiceResponse updateService(
            Long serviceId,
            ExpertServiceRequest expertServiceRequest
    );

    Void deleteService(Long serviceId);

    List<ExpertServiceResponse> getAllMyServiceByOpend();

    List<ExpertServiceResponse> getAllPublicServices();

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