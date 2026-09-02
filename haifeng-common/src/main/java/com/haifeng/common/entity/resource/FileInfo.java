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

    /** 带水印PDF的OSS对象key（生成成功后缓存复用；水印文本固定，故一个源文件只需生成一次） */
    private String watermarkedFileUrl;

    /** 水印PDF生成状态：NONE-未生成 PENDING-生成中 READY-已就绪 FAILED-生成失败（降级返回原文件） */
    private String watermarkStatus;

    /** 水印PDF生成失败原因（排查用，不对外暴露） */
    private String watermarkFailReason;

    /** 面向人群（middle_school:初中生, high_school:高中生） */
    private String targetAudience;

    /** 适合人群（初一/初二/高一/高二等） */
    private String applicableStage;

    /** 学科（数学/语文/英语等） */
    private String subject;

    /** 文档简介（备考指南、政策说明等） */
    private String description;

    /** 标签（备考指南/就业辅导等，用于精准查询） */
    private String tag;

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
