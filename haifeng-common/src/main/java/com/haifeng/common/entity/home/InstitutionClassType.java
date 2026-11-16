package com.haifeng.common.entity.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 培训机构主打班型（存 t_institutions.class_types JSONB 数组）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionClassType implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "班型标题不能为空")
    @Size(max = 64, message = "班型标题最长64字符")
    private String title;

    @Size(max = 256, message = "班型介绍最长256字符")
    private String introduction;

    @Size(max = 32, message = "班型标签最长32字符")
    private String tag;
}
