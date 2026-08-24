package com.haifeng.admin.dto.fileload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileLoadUploadDTO {

    /** 面向人群（middle_school/high_school） */
    @NotBlank(message = "面向人群不能为空")
    private String targetAudience;

    /** 学科（数学/语文/英语等） */
    @NotBlank(message = "学科不能为空")
    @Size(max = 50, message = "学科最长50字符")
    private String subject;

    /** 适合人群（初一/初二/高一/高二等） */
    @Size(max = 20, message = "适合人群最长20字符")
    private String applicableStage;

    /** 文档简介 */
    private String description;

    /** 标签（备考指南/就业辅导等，用于精准查询） */
    @Size(max = 100, message = "标签最长100字符")
    private String tag;

    /** 乐观锁版本号（修改时必传） */
    private Integer version;
}
