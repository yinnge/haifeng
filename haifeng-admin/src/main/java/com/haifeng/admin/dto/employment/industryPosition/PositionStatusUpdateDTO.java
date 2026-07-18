package com.haifeng.admin.dto.employment.industryPosition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PositionStatusUpdateDTO {
    @NotBlank(message = "岗位状态不能为空")
    @Size(max = 20, message = "岗位状态长度不能超过20")
    private String positionStatus;
}
