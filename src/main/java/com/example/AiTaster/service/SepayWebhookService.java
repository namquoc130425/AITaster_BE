package com.example.AiTaster.service;


import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SepayWebhookService {
    private static final Pattern PAYMENT_CODE_PATTERN = Pattern.compile("AIT-PROJ-\\d+-[A-Z0-9]{8}", Pattern.CASE_INSENSITIVE);

    @Value("${app.sepay.webhook-secret}")
    private String webhookSecret;

    private final ObjectMapper objectMapper;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;


    //paseBody chuyển rawboy sang SePayRequest
    private SepayWebhookRequest parseBody(String rawBody) {
        try{
          return  objectMapper.readValue(rawBody,SepayWebhookRequest.class);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook body");
        }
    }
    //chỉnh lại signature thành viết thường và xóa chứ sha256 ở đầu chuổi
    private String nnormalizeSignature(String signature) {
        return signature.trim().replaceFirst("(?i)^sha256=","").toLowerCase();
    }


    // lấy chuổi đầu tiên không null và không rỗng
    private String firstNotBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }

}
