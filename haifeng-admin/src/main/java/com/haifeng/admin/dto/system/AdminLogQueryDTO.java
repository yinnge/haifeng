package com.haifeng.admin.dto.system;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminLogQueryDTO extends BasePageQueryDTO {

    /**
     * 管理员姓名（模糊查询）
     */
    private String adminName;

    /**
     * 操作结果：SUCCESS/FAIL
     */
    private String result;

    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    private String requestMethod;
}
