package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementQueryDTO extends BasePageQueryDTO {
    private String title;  // 标题模糊查询
    private Short status;  // 状态筛选: 0-下架 1-展示
}
