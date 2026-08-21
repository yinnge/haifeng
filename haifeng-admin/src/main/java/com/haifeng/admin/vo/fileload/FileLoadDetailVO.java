package com.haifeng.admin.vo.fileload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FileLoadDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String fileName;

    private String fileUrl;

    private String filePreviewUrl;

    private String fileType;

    private Long fileSize;

    private String fileMd5;

    private String bucketName;

    private String targetAudience;

    private String applicableStage;

    private String subject;

    private Integer version;

    private String createBy;

    private OffsetDateTime createTime;

    private String updateBy;

    private OffsetDateTime updateTime;
}
