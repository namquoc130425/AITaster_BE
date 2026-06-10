package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;

public interface IExpertService {
     ExpertServiceResponse CreatService(ExpertServiceRequest expertServiceRequest);


    ExpertServiceResponse updateService(Long serviceId, ExpertServiceRequest expertServiceRequest);

    ExpertServiceResponse getServiceById(Long serviceId);

    Void deleteService(Long serviceId);

    ExpertServiceResponse getAllMyService(Long userId);




}
