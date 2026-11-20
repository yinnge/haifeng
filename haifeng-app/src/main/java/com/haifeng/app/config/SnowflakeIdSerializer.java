package com.haifeng.app.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

/**
 * 全局雪花 ID 序列化兜底（C 端 haifeng-app）。
 *
 * <p>问题背景：业务表主键为 19 位雪花 Long，超出 JavaScript
 * {@code Number.MAX_SAFE_INTEGER}（9007199254740991）。若以 JSON number 下发，
 * 浏览器端未被 json-bigint 覆盖的路径会发生精度丢失（末位被抹平），
 * 导致收藏、详情跳转等按 ID 查询的接口命中不到记录（如收藏报"目标不存在或已下架"）。
 *
 * <p>策略：仅当 Long 值超出 JS 安全整数范围时才写成 JSON 字符串；
 * 计数、字节大小、时间戳等小数字仍写成 number，避免打破前端 number 类型假设。
 * 字段级 {@code @JsonSerialize(using = ToStringSerializer.class)} 优先级更高，
 * 与已有 84 个 VO 注解行为一致，不会冲突。
 *
 * <p>该配置由 Spring Boot 自动注册到 ObjectMapper，作用于所有 Controller 响应。
 */
@JsonComponent
public class SnowflakeIdSerializer extends StdSerializer<Long> {

    /** JavaScript Number.MAX_SAFE_INTEGER = 2^53 - 1 */
    private static final long JS_MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    public SnowflakeIdSerializer() {
        super(Long.class);
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (value > JS_MAX_SAFE_INTEGER || value < -JS_MAX_SAFE_INTEGER) {
            gen.writeString(value.toString());
        } else {
            gen.writeNumber(value);
        }
    }
}
