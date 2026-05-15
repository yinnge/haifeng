package com.haifeng.common.service.algorithm.matcher;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectMatchResult {
    private boolean match;
    private String reason;

    public static SubjectMatchResult ok() {
        return SubjectMatchResult.builder().match(true).build();
    }

    public static SubjectMatchResult fail(String reason) {
        return SubjectMatchResult.builder().match(false).reason(reason).build();
    }
}
