package com.haifeng.admin.vo.employment.contentManagement.notice;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class NoticeListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String noticeCategory;
    private String noticeType;
    private String province;
    private String city;
    private String year;
    private Boolean isTop;
    private Boolean isImportant;
    private Integer viewCount;
    private Integer sortOrder;
}
