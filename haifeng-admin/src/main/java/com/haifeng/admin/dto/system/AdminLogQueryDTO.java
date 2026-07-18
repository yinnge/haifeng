package com.haifeng.admin.dto.system;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminLogQueryDTO extends BasePageQueryDTO {

    /**
     * 管理员姓名（模糊查询）
     */
    @Size(max = 50, message = "管理员姓名长度不能超过50")
    private String adminName;

    /**
     * 操作结果：SUCCESS/FAIL
     */
    @Size(max = 20, message = "操作结果长度不能超过20")
    private String result;

    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    @Size(max = 10, message = "请求方法长度不能超过10")
    private String requestMethod;
}
