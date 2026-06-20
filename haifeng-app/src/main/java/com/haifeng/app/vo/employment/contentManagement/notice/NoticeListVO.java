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
public class NoticeListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String summary;

    private OffsetDateTime publishDate;

    private Integer viewCount;

    private String noticeCategory;

    private String province;

    private String city;

    private String year;

    private OffsetDateTime regStartDate;

    private OffsetDateTime regEndDate;

    private Integer recruitmentCount;
}
