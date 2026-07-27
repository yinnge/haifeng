package com.haifeng.admin.service.dashboard;

import com.haifeng.admin.vo.dashboard.DashboardStatsVO;
import com.haifeng.admin.vo.dashboard.TrendDataVO;

public interface DashboardService {
    DashboardStatsVO getDashboardStats();

    /**
     * 获取用户增长趋势
     * @param days 天数，限制为 7/30/90
     */
    TrendDataVO getMemberTrend(int days);

    /**
     * 获取订单趋势
     * @param days 天数，限制为 7/30/90
     */
    TrendDataVO getOrderTrend(int days);
}
