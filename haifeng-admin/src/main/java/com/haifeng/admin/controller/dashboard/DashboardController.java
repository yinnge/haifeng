package com.haifeng.admin.controller.dashboard;

import com.haifeng.admin.service.dashboard.DashboardService;
import com.haifeng.admin.vo.dashboard.DashboardOverviewVO;
import com.haifeng.admin.vo.dashboard.DashboardStatsVO;
import com.haifeng.admin.vo.dashboard.TrendDataVO;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @RequireAdminModule("dashboard")
    public R<DashboardStatsVO> getStats() {
        return R.ok(dashboardService.getDashboardStats());
    }

    /**
     * 获取用户增长趋势
     * @param days 天数：7/30/90，默认7
     */
    @GetMapping("/member-trend")
    @RequireAdminModule("dashboard")
    public R<TrendDataVO> getMemberTrend(
            @RequestParam(defaultValue = "7") int days) {
        return R.ok(dashboardService.getMemberTrend(days));
    }

    /**
     * 获取订单趋势
     * @param days 天数：7/30/90，默认7
     */
    @GetMapping("/order-trend")
    @RequireAdminModule("dashboard")
    public R<TrendDataVO> getOrderTrend(
            @RequestParam(defaultValue = "7") int days) {
        return R.ok(dashboardService.getOrderTrend(days));
    }

    /**
     * 获取仪表盘概览（系统信息 + 待办事项）
     */
    @GetMapping("/overview")
    @RequireAdminModule("dashboard")
    public R<DashboardOverviewVO> getOverview() {
        return R.ok(dashboardService.getDashboardOverview());
    }
}
