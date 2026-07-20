package com.haifeng.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 在 Spring Boot 启动最早期加载项目根目录下的 .env 文件，
 * 使其中的 key=value 对可被 application*.yml 中的 ${...} 占位符解析。
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envPath = findDotenvFile();
        if (envPath == null || !Files.exists(envPath)) {
            return;
        }

        Map<String, Object> properties = parse(envPath);
        if (!properties.isEmpty()) {
            environment.getPropertySources()
                    .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private Path findDotenvFile() {
        // 依次尝试：项目根目录、当前目录、haifeng-app/上级、haifeng-admin/上级
        String[] candidates = {
                "../.env",
                ".env",
                "../../.env"
        };
        for (String candidate : candidates) {
            Path path = Paths.get(candidate).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    private Map<String, Object> parse(Path path) {
        Map<String, Object> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    if (!key.isEmpty()) {
                        result.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            // 静默忽略，不阻塞启动
        }
        return result;
    }
}
