package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Map 阶段单条结果（序列化为 JSONB 存入 map_results 数组）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapResultItem {

    private Long universityId;
    private String universityName;
    private String cityName;
    private String groupName;
    private List<MajorBrief> majors;

    /** AI 产出的 ~300字简评；失败时为 null */
    private String commentary;

    /** AI 调用是否成功 */
    private Boolean success;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorBrief {
        private String majorName;
        private BigDecimal safetyLevel;
        private String levelShort;
    }
}
