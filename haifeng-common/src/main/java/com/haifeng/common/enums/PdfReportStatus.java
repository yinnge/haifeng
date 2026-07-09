package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PdfReportStatus {
    GENERATING((short) 0, "生成中"),
    SUCCESS((short) 1, "成功"),
    FAILED((short) 2, "失败");

    @EnumValue
    private final short value;
    private final String desc;
}
