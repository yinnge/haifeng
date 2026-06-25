package com.haifeng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek（OpenAI 兼容协议）配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    /** DeepSeek API base URL */
    private String baseUrl = "https://api.deepseek.com";

    /** 模型名 */
    private String model = "deepseek-v4-flash";

    /** API key 列表（application.yml 中用逗号分隔，Spring 自动拆分） */
    private List<String> apiKeys = new ArrayList<>();

    /** max_tokens */
    private Integer maxTokens = 4096;

    /** temperature */
    private Double temperature = 1.0;

    /** 单 key 失败后的冷却秒数 */
    private Integer keyCooldownSeconds = 300;

    /** 请求超时秒数 */
    private Integer timeoutSeconds = 60;
}
