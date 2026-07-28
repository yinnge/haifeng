package com.haifeng.admin.vo.dashboard;

import lombok.Data;

@Data
public class SystemInfoVO {
    /** 应用版本 */
    private String appVersion;
    /** Spring Boot 版本 */
    private String springVersion;
    /** Java 版本 */
    private String javaVersion;
    /** 站点名称 */
    private String siteName;
    /** AI 提供商 */
    private String aiProvider;
    /** AI 模型 */
    private String aiModel;
    /** 管理员数量 */
    private Long adminCount;
}
