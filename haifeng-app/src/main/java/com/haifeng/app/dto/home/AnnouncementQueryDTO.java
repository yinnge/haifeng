package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementQueryDTO extends BasePageQueryDTO {

    /** 标签精准匹配（可选） */
    @Size(max = 50, message = "标签最长50个字符")
    private String tag;
}
