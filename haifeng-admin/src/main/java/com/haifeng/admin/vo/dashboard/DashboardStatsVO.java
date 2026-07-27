package com.haifeng.admin.vo.dashboard;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardStatsVO {
    private MemberStats memberStats;
    private OrderStats orderStats;
    private EntityStats entityStats;

    @Data
    public static class MemberStats {
        private Long totalMembers;
        private Long proMembers;
        private Long vipMembers;
    }

    @Data
    public static class OrderStats {
        private Long pendingOrders;
        private BigDecimal totalAmount;
    }

    @Data
    public static class EntityStats {
        private Long universityCount;
        private Long majorCount;
        private Long industryCount;
        private Long enterpriseCount;
        private Long admissionGroupCount;
        private Long admissionMajorScoreCount;
    }
}
