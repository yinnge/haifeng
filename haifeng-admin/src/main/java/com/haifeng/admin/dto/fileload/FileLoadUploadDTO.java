package com.haifeng.admin.dto.fileload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileLoadUploadDTO {

    /** 面向人群（middle_school/high_school/college） */
    @NotBlank(message = "面向人群不能为空")
    private String targetAudience;

    /** 存储方式：OSS-上传文件（默认） / BAIDU-百度网盘链接，新增网盘资源时必传 BAIDU */
    private String storageType;

    /** 资源名称（OSS 上传取自文件名；BAIDU 网盘录入时必填，编辑时可改名） */
    @Size(max = 255, message = "资源名称最长255字符")
    private String fileName;

    /** 百度网盘分享链接（storageType=BAIDU 时必填） */
    @Size(max = 1024, message = "网盘链接最长1024字符")
    private String netdiskUrl;

    /** 百度网盘提取码（storageType=BAIDU 时可空） */
    @Size(max = 20, message = "提取码最长20字符")
    private String accessCode;

    /** 封面图URL（可空，默认封面由前端渲染） */
    @Size(max = 500, message = "封面URL最长500字符")
    private String coverUrl;

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
