package com.example.travelez.backend.infrastructure.captcha.impl;

import com.example.travelez.backend.infrastructure.captcha.CaptchaService;
import com.example.travelez.backend.infrastructure.captcha.dto.RecaptchaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleRecaptchaService implements CaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public GoogleRecaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean verify(String recaptchaToken) {

        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            return false;
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecret);
        requestMap.add("response", recaptchaToken);

        RecaptchaResponse response = restTemplate.postForObject(recaptchaVerifyUrl, requestMap, RecaptchaResponse.class);
        return response != null && response.isSuccess();
    }
}
