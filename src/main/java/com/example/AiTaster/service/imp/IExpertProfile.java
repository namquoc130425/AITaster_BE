package com.example.AiTaster.service.imp;


import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;

import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.User;

import java.util.List;

public interface IExpertProfile {
    List<ExpertProfileResponse> getAll();


    ExpertProfileResponse getByExpertId(Long expertId);



    ExpertProfileResponse getByUserId(Long userId);



    ExpertProfileResponse createForRegister(User user, ExpertRegisterRequest request
    );

    ExpertProfileResponse update(Long id, ExpertProfileRequest request
    );



   void  delete(Long id);
}
