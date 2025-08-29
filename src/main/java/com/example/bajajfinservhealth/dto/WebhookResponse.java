package com.example.bajajfinservhealth.dto;

import lombok.Data;

@Data
public class WebhookResponse {
    private String webhookUrl;
    private String accessToken;
}
