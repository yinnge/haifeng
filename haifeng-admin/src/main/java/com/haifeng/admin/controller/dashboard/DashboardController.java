package com.haifeng.admin.controller.dashboard;

import com.haifeng.admin.service.dashboard.DashboardService;
import com.haifeng.admin.vo.dashboard.DashboardStatsVO;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
