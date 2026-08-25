package com.haifeng.admin.dto.fileload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * OSS 上传确认请求 DTO
 * 前端直传 OSS 成功后，调用此接口保存文件元数据
 */
@Data
public class OssConfirmUploadDTO {

    /** OSS 对象 key（从预签名响应中获取） */
    @NotBlank(message = "objectKey 不能为空")
    private String objectKey;

    /** 原始文件名 */
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /** 文件大小（字节） */
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

    /** 文件 MD5（可选，前端可计算） */
    private String fileMd5;

    /** 面向人群（middle_school/high_school） */
    @NotBlank(message = "面向人群不能为空")
    private String targetAudience;

    /** 学科 */
    @NotBlank(message = "学科不能为空")
    private String subject;

    /** 适合人群 */
    private String applicableStage;

    /** 文档简介 */
    private String description;

    /** 标签 */
    private String tag;
}
