package com.haifeng.app.vo.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端资源列表 VO（任务 3 接口 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String resourceName;
    private String coverUrl;
    private String description;
    private String category;
    private String fileType;
    private Integer viewCount;
}
