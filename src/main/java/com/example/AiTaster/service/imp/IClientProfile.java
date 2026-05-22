package com.example.AiTaster.service.imp;


import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.entity.ClientProfile;

import java.util.List;

public interface IClientProfile {

    List<ClientProfileResponse> getAll();


    ClientProfileResponse getByClientId(Long clientId);

    ClientProfileResponse getByUserId(Long userId);

    ClientProfileResponse create(ClientProfileRequest request);

    ClientProfileResponse update(Long id, ClientProfileRequest request);

    void delete(Long id);
}
