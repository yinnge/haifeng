package com.haifeng.app.vo.employment.contentManagement.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private Integer viewCount;

    private Integer sortOrder;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
