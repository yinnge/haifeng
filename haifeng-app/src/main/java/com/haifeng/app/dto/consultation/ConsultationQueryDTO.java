package com.haifeng.app.dto.consultation;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教师端收件箱查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsultationQueryDTO extends BasePageQueryDTO {

    /**
     * 处理状态：0-待处理 1-已处理，不传=全部
     */
    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 1, message = "状态值最大为1")
    private Integer status;

    /**
     * 业务类型：planner / institution，不传=全部
     */
    @Size(max = 32, message = "业务类型最长32字符")
    private String bizType;
}
