package com.haifeng.app.vo.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端资源 URL VO（任务 3 接口 2，登录后查看）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUrlVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String resourceUrl;
    private String accessCode;
}
