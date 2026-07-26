package com.haifeng.admin.vo.employment.contentManagement.notice;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class NoticeDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String noticeCategory;
    private String noticeType;
    private String title;
    private String summary;
    private String content;
    private String province;
    private String city;
    private String[] tags;
    private String year;
    private String source;
    private String sourceUrl;
    private OffsetDateTime publishDate;
    private String publishUnit;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private Integer recruitmentCount;
    private Boolean isTop;
    private Boolean isImportant;
    private Integer sortOrder;
    private Integer viewCount;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
