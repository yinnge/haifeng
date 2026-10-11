package com.haifeng.admin.vo.fileload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FileLoadListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String fileName;

    /** 存储方式：OSS-对象存储 / BAIDU-百度网盘 */
    private String storageType;

    /** 封面图URL（BAIDU 网盘资源可空） */
    private String coverUrl;

    private String fileType;

    private Long fileSize;

    private String subject;

    private String applicableStage;

    private String description;

    private String tag;

    private String createBy;

    private OffsetDateTime createTime;
}
