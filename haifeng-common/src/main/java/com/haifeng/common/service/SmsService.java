package com.haifeng.common.service;

public interface SmsService {
    String sendSmsCode(String phone, String code);
}
