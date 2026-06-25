package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;

public interface IProjectPayment {
    ProjectPaymentResponse createProjectPayment(Long projectId);

}
