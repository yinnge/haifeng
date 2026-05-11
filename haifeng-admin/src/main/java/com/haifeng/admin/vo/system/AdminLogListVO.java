package com.haifeng.admin.vo.system;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AdminLogListVO {

    private Long id;

    /**
     * 管理员姓名
     */
    private String adminName;

    /**
     * 操作描述
     */
    private String operation;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 操作结果
     */
    private String result;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}
