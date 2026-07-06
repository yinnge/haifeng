package com.haifeng.admin.dto.employment.contentManagement.notice;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQueryDTO extends BasePageQueryDTO {
    private String title;
    private String noticeCategory;
    private String noticeType;
    private String province;
    private String city;
    private String year;
    private Boolean isTop;
    private Boolean isImportant;
}
