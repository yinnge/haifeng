package com.haifeng.admin.vo.home;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class AnnouncementDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String content;
    private String tag;
    private Short status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
