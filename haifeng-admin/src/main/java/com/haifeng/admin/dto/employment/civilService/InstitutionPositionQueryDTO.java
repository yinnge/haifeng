package com.haifeng.admin.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionPositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "职位名称最多50字符")
    private String positionName;
    @Size(max = 50, message = "主管部门最多50字符")
    private String supervisingDept;
    @Size(max = 50, message = "事业单位最多50字符")
    private String institution;
    @Size(max = 20, message = "省份最多20字符")
    private String province;
    @Size(max = 20, message = "考试类别最多20字符")
    private String examCategory;
    @Size(max = 20, message = "岗位类型最多20字符")
    private String positionType;
    @Size(max = 20, message = "岗位状态最多20字符")
    private String positionStatus;
}
