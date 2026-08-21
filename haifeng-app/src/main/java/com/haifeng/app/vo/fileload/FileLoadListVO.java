package com.haifeng.app.vo.fileload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FileLoadListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String subject;

    private String applicableStage;

    private OffsetDateTime createTime;
}
