package com.example.AiTaster.service;

import com.example.AiTaster.exception.GlobalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SepayBankGateway {
    private final ObjectMapper objectMapper;

    @Value("${app.sepay.payout-url:}")
    private String payoutUrl;

    @Value("${app.sepay.api-key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SepayPayoutResult createPayout(
            String bankCode,
            String accountNumber,
            String accountHolderName,
            BigDecimal amount,
            String currency,
            String requestCode,
            String description
    ) {
        if (isBlank(payoutUrl) || isBlank(apiKey)) {
            return SepayPayoutResult.builder()
                    .providerName("SEPAY_LOCAL_SANDBOX")
                    .providerTransactionCode("LOCAL-" + requestCode)
                    .rawResponse("SePay payout endpoint is not configured")
                    .build();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("bankCode", bankCode);
        payload.put("accountNumber", accountNumber);
        payload.put("accountHolderName", accountHolderName);
        payload.put("amount", amount);
        payload.put("currency", currency);
        payload.put("requestCode", requestCode);
        payload.put("description", description);

        JsonNode response = postJson(payoutUrl, payload);

        if (!isSuccessfulResponse(response)) {
            throw new GlobalException(400, "SePay payout request failed");
        }

        return SepayPayoutResult.builder()
                .providerName("SEPAY")
                .providerTransactionCode(firstText(
                        response,
                        "transactionId",
                        "transaction_id",
                        "providerTransactionCode",
                        "code"
                ))
                .rawResponse(response.toString())
                .build();
    }

    private JsonNode postJson(String url, Map<String, Object> payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new GlobalException(400, "SePay request failed");
            }

            return objectMapper.readTree(response.body());
        } catch (GlobalException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GlobalException(500, "Cannot call SePay bank API");
        }
    }

    private boolean isSuccessfulResponse(JsonNode response) {
        JsonNode success = response.path("success");
        if (success.isBoolean()) {
            return success.asBoolean();
        }

        JsonNode valid = response.path("valid");
        if (valid.isBoolean()) {
            return valid.asBoolean();
        }

        String status = firstText(response, "status", "code");
        return status == null
                || status.equalsIgnoreCase("success")
                || status.equalsIgnoreCase("ok")
                || status.equals("200");
    }

    private String firstText(JsonNode response, String... keys) {
        for (String key : keys) {
            String value = textAt(response, key);
            if (!isBlank(value)) {
                return value;
            }
        }

        JsonNode data = response.path("data");
        for (String key : keys) {
            String value = textAt(data, key);
            if (!isBlank(value)) {
                return value;
            }
        }

        return null;
    }

    private String textAt(JsonNode node, String key) {
        JsonNode value = node.path(key);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Data
    @Builder
    public static class SepayPayoutResult {
        String providerName;
        String providerTransactionCode;
        String rawResponse;
    }
}
