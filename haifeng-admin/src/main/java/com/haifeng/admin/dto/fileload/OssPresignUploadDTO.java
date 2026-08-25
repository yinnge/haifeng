package com.haifeng.admin.dto.fileload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * OSS 预签名上传请求 DTO
 */
@Data
public class OssPresignUploadDTO {

    /** 原始文件名（用于提取扩展名） */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名最长255字符")
    private String fileName;

    /** 文件大小（字节），用于前端展示和后端校验 */
    private Long fileSize;

    /** 面向人群（middle_school/high_school） */
    @NotBlank(message = "面向人群不能为空")
    private String targetAudience;

    /** 学科 */
    @NotBlank(message = "学科不能为空")
    @Size(max = 50, message = "学科最长50字符")
    private String subject;

    /** 适合人群 */
    @Size(max = 20, message = "适合人群最长20字符")
    private String applicableStage;

    /** 文档简介 */
    private String description;

    /** 标签 */
    @Size(max = 100, message = "标签最长100字符")
    private String tag;
}
