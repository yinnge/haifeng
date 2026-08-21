package com.haifeng.admin.controller.system;

import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import com.haifeng.admin.vo.system.SystemResourceVO;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 系统管理 - 系统监控（主机资源检测）
 * <p>
 * 提供本地 / 服务器通用的 CPU 与物理内存使用率检测。
 * 数据通过 JVM 内置的 {@link OperatingSystemMXBean} 读取，无需任何第三方依赖，
 * Windows / Linux 表现一致。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/system/monitor")
@Validated
@RequireAdminModule("system_monitor")
public class SystemMonitorController {

    /** 1 GB 的字节数 */
    private static final double BYTES_PER_GB = 1024.0 * 1024.0 * 1024.0;

    /**
     * 获取主机资源使用情况（CPU + 物理内存）
     */
    @GetMapping("/resource")
    public R<SystemResourceVO> resource() {
        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long totalBytes = osBean.getTotalPhysicalMemorySize();
        long freeBytes = osBean.getFreePhysicalMemorySize();
        long usedBytes = totalBytes - freeBytes;

        // getSystemCpuLoad() 返回 0~1 的系统 CPU 占用；不支持的环境返回 -1
        double cpuLoad = osBean.getSystemCpuLoad();

        SystemResourceVO vo = new SystemResourceVO();
        vo.setOsName(System.getProperty("os.name"));
        vo.setOsArch(System.getProperty("os.arch"));
        vo.setCpuCores(osBean.getAvailableProcessors());
        vo.setTotalMemoryGb(toGb(totalBytes));
        vo.setUsedMemoryGb(toGb(usedBytes));
        vo.setFreeMemoryGb(toGb(freeBytes));
        vo.setMemoryUsageRate(totalBytes > 0 ? percent(usedBytes, totalBytes) : null);
        vo.setCpuUsageRate(cpuLoad >= 0 ? percent((long) (cpuLoad * 100), 100) : null);
        vo.setTimestamp(System.currentTimeMillis());

        return R.ok(vo);
    }

    private BigDecimal toGb(long bytes) {
        return BigDecimal.valueOf(bytes / BYTES_PER_GB).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(double part, double whole) {
        if (whole <= 0) {
            return null;
        }
        return BigDecimal.valueOf(part / whole * 100).setScale(2, RoundingMode.HALF_UP);
    }
}
