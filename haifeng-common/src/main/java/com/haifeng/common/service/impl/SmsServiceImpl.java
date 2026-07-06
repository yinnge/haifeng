package com.haifeng.common.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.common.config.SmsProperties;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.SmsService;
import com.haifeng.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final WebClient webClient;
    private final SmsProperties smsProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String sendSmsCode(String phone, String code) {
        String content = smsProperties.getSign() + "您的验证码是：" + code + "，5分钟内有效，请勿泄露他人。";

        try {
            String response = webClient.post()
                    .uri("https://api-v4.mysubmail.com/sms/send.json")
                    .bodyValue(Map.of(
                            "appid", smsProperties.getAppid(),
                            "to", phone,
                            "content", content,
                            "signature", smsProperties.getAppkey()
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            JsonNode json = objectMapper.readTree(response);
            String status = json.has("status") ? json.get("status").asText() : null;
            String sendId = json.has("send_id") ? json.get("send_id").asText() : null;

            if (!"success".equals(status)) {
                String msg = json.has("msg") ? json.get("msg").asText() : "unknown";
                log.error("发送短信验证码失败，phone={}, status={}, msg={}",
                        DesensitizeUtil.desensitizePhone(phone), status, msg);
                throw new BusinessException(ResultCode.SMS_SEND_FAILED);
            }

            log.info("发送短信验证码成功，phone={}, sendId={}",
                    DesensitizeUtil.desensitizePhone(phone), sendId);
            return sendId;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送短信验证码失败，phone={}", DesensitizeUtil.desensitizePhone(phone), e);
            throw new BusinessException(ResultCode.SMS_SEND_FAILED, "短信发送失败，请稍后重试");
        }
    }
}
