package com.haifeng.admin.dto.employment.contentManagement.notice;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class NoticeUpdateDTO {
    private String noticeCategory;
    private String noticeType;
    private String title;
    private String summary;
    private String content;
    private String province;
    private String city;
    private String[] tags;
    private Integer year;
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
}
