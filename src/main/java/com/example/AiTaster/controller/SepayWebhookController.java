package com.example.AiTaster.controller;

import com.example.AiTaster.service.SepayWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/sepay")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SepayWebhookController {
    private final SepayWebhookService sepayWebhookService;

    @PostMapping
    public ResponseEntity<?> handleSepayIpn(
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKey,
            @RequestBody String rawBody
    ) {
        sepayWebhookService.handleWebhook(rawBody, secretKey);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "IPN received"
        ));
    }
}
