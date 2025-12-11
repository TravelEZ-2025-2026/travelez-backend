package com.example.travelez.backend.infrastructure.captcha;

public interface CaptchaService {
    boolean verify(String recaptchaToken);
}

