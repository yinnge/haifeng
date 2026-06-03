package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class AnnouncementListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String tag;
    private OffsetDateTime updatedAt;
}
