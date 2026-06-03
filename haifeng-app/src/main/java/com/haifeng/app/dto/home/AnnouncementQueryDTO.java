package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementQueryDTO extends BasePageQueryDTO {

    /** 标签精准匹配（可选） */
    private String tag;
}
