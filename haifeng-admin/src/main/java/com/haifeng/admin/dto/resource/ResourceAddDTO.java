package com.haifeng.admin.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResourceAddDTO {

    @NotBlank(message = "资源名称不能为空")
    @Size(max = 100, message = "资源名称最长100字符")
    private String resourceName;

    @Size(max = 500, message = "封面URL最长500字符")
    private String coverUrl;

    @Size(max = 1000, message = "描述最长1000字符")
    private String description;

    @NotBlank(message = "资源URL不能为空")
    @Size(max = 500, message = "资源URL最长500字符")
    private String resourceUrl;

    @Size(max = 50, message = "访问码最长50字符")
    private String accessCode;

    @Size(max = 50, message = "分类最长50字符")
    private String category;

    @Size(max = 20, message = "文件类型最长20字符")
    private String fileType;

    /**
     * 排序序号，默认0
     */
    private Integer sortOrder;
}
