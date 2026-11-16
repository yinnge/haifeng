package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlannerQueryDTO extends BasePageQueryDTO {

    /** 服务对象：高考 / 考研（必填，前端两个页面各自传固定值） */
    @NotBlank(message = "服务对象不能为空")
    @Size(max = 32, message = "服务对象最长32字符")
    private String serviceTarget;

    /** 所属省份，必须是 ProvinceEnum 中文 desc（可选） */
    @Size(max = 30, message = "省份最长30个字符")
    private String province;
}
