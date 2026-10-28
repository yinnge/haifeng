package com.haifeng.app.dto.fileload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 文件资料（高考/中考/考公/考研/大学等）C 端列表分页 DTO。
 * <p>与全局 BasePageQueryDTO 解耦：默认每页 12 条，允许档位 12/24/36/58/110；
 * 因 110 超出 BasePageQueryDTO 的 @Max(100) 全局契约，故独立定义，不影响其它分页接口。
 */
@Data
public class FileLoadPageQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 12, message = "每页最小12条")
    @Max(value = 110, message = "每页最大110条")
    private Integer size = 12;
}
