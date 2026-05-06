package com.haifeng.admin.vo.home;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class AnnouncementDetailVO {
    private Long id;
    private String title;
    private String content;
    private String tag;
    private Short status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
