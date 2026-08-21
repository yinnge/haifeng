package com.haifeng.admin.vo.system;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 主机资源使用情况
 * 数据来自 java.lang.management + com.sun.management.OperatingSystemMXBean，
 * 跨平台（Windows / Linux）通用，反映「运行后端 JVM 的那台机器」的真实资源占用。
 */
@Data
public class SystemResourceVO {

    /** 操作系统名称，如 Windows 11 / Linux */
    private String osName;

    /** 系统架构，如 amd64 / aarch64 */
    private String osArch;

    /** CPU 逻辑核心数 */
    private Integer cpuCores;

    /** 物理内存总量（GB） */
    private BigDecimal totalMemoryGb;

    /** 已使用物理内存（GB） */
    private BigDecimal usedMemoryGb;

    /** 空闲物理内存（GB） */
    private BigDecimal freeMemoryGb;

    /** 内存使用率（%，0~100，保留两位小数） */
    private BigDecimal memoryUsageRate;

    /**
     * CPU 使用率（%，0~100，保留两位小数）
     * 注意：部分容器/JDK 环境不支持系统级 CPU 采样时返回 null
     */
    private BigDecimal cpuUsageRate;

    /** 采样时间戳（毫秒） */
    private Long timestamp;
}
