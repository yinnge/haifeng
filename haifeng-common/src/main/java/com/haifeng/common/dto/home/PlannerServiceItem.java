package com.haifeng.common.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 规划师服务内容条目（services JSONB 数组元素）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannerServiceItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标题，例：首次诊断 */
    @NotBlank(message = "服务标题不能为空")
    @Size(max = 64, message = "服务标题最长64字符")
    private String title;

    /** 内容，例：成绩位次、学科优势、家庭诉求三维评估…… */
    @NotBlank(message = "服务内容不能为空")
    @Size(max = 1024, message = "服务内容最长1024字符")
    private String content;
}
