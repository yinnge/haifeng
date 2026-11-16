package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Admin 端咨询留言查询（只读）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsultationQueryDTO extends BasePageQueryDTO {

    /**
     * 接收教师会员ID精确筛选
     */
    private Long teacherMemberId;

    /**
     * 业务类型：planner / institution，不传=全部
     */
    @Size(max = 32, message = "业务类型最长32字符")
    private String bizType;

    /**
     * 业务ID精确筛选
     */
    private Long bizId;

    /**
     * 处理状态：0-待处理 1-已处理，不传=全部
     */
    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 1, message = "状态值最大为1")
    private Integer status;

    /**
     * 联系电话精确筛选
     */
    @Size(max = 20, message = "手机号最长20字符")
    private String phone;
}
