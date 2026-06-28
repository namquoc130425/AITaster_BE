package com.example.AiTaster.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepayCheckoutFormResponse {
    // URL form sẽ submit sang SePay.
    private String actionUrl;

    // Method submit form, theo docs là POST.
    private String method;

    // Danh sách hidden input gửi sang SePay.
    private Map<String, String> fields;
}
