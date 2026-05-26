package com.example.AiTaster.service.imp;



import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.request.ClientRegisterRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.User;
import org.mapstruct.control.MappingControl;

import java.util.List;

public interface    IClientProfile {

    List<ClientProfileResponse> getAll();


    ClientProfileResponse getByClientId(Long clientId);

    ClientProfileResponse getByUserId(Long userId);



    ClientProfileResponse createForRegister(
            User user,
            ClientRegisterRequest request
    );

    ClientProfileResponse update(
            Long id,
            ClientProfileRequest request
    );



    void delete(Long id);
}
