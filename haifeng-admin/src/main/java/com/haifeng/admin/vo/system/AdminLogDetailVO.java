package com.haifeng.admin.vo.system;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AdminLogDetailVO {

    private Long id;

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 管理员姓名
     */
    private String adminName;

    /**
     * 操作描述
     */
    private String operation;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 操作结果
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}
