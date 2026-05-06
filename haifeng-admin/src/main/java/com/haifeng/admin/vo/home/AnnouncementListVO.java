package com.haifeng.admin.vo.home;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class AnnouncementListVO {
    private Long id;
    private String title;
    private String tag;
    private Short status;
    private OffsetDateTime updatedAt;
}
