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

    /** 存储方式：OSS-对象存储 / BAIDU-百度网盘 */
    private String storageType;

    /** 百度网盘分享链接（BAIDU 行；管理端可查看编辑） */
    private String netdiskUrl;

    /** 百度网盘提取码（BAIDU 行） */
    private String accessCode;

    /** 封面图URL（BAIDU 网盘资源可空） */
    private String coverUrl;

    private String fileType;

    private Long fileSize;

    private String fileMd5;

    private String bucketName;

    private String targetAudience;

    private String applicableStage;

    private String subject;

    private String description;

    private String tag;

    private Integer version;

    private String createBy;

    private OffsetDateTime createTime;

    private String updateBy;

    private OffsetDateTime updateTime;
}
