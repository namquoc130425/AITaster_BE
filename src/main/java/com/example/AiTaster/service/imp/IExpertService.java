package com.example.AiTaster.service.imp;

import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.ServiceFile;

import java.util.List;

public interface IExpertService {
     ExpertServiceResponse CreatService(ExpertServiceRequest expertServiceRequest);


    ExpertServiceResponse updateService(Long serviceId, ExpertServiceRequest expertServiceRequest);


    Void deleteService(Long serviceId);

    List<ExpertServiceResponse> getAllMyServiceByOpend();

    List<ExpertServiceResponse> getAllPublicServices();

    ExpertServiceResponse  getPublicServiceDetail(long serviceId);

    // Đổi trạng thái service.
    ExpertServiceResponse changeServiceStatus(Long serviceId, ServiceStatus serviceStatus);
    ExpertServiceResponse getMyServiceDetail(Long serviceId);




}
