package com.haifeng.common.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_announcements")
public class Announcement {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String content;

    private String tag;

    private Short status;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
