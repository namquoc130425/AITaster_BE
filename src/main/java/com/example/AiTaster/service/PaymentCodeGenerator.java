package com.example.AiTaster.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentCodeGenerator {

    public String generate(String prefix, Long referenceId) {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return prefix + "-" + referenceId + "-" + randomPart;
    }
}
