package com.haifeng.common.entity.resource;

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
@TableName("t_file_info")
public class FileInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String fileName;

    private String fileUrl;

    private String filePreviewUrl;

    private String fileType;

    private Long fileSize;

    private String fileMd5;

    private String bucketName;

    /** 面向人群（middle_school:初中生, high_school:高中生） */
    private String targetAudience;

    /** 适合人群（初一/初二/高一/高二等） */
    private String applicableStage;

    /** 学科（数学/语文/英语等） */
    private String subject;

    @Version
    private Integer version;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;
}
