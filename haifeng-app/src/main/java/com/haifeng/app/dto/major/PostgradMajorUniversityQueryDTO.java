package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorUniversityQueryDTO extends BasePageQueryDTO {

    @Size(max = 50, message = "院校类型最长50个字符")
    private String category;
}
