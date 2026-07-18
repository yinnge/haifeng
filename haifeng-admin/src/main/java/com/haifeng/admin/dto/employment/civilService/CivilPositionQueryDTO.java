package com.haifeng.admin.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CivilPositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "职位名称最多50字符")
    private String positionName;
    @Size(max = 50, message = "招录部门最多50字符")
    private String recruitingDept;
    @Size(max = 50, message = "工作地点最多50字符")
    private String workLocation;
    @Size(max = 20, message = "考试类型最多20字符")
    private String examType;
    @Size(max = 20, message = "报名状态最多20字符")
    private String regStatus;
    @Size(max = 20, message = "最低学历最多20字符")
    private String minEducation;
}
