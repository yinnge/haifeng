package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置/撤销老师身份
 */
@Data
public class MemberTeacherDTO {

    /**
     * true=授予老师身份，false=撤销老师身份
     */
    @NotNull(message = "老师身份标记不能为空")
    private Boolean teacher;
}
