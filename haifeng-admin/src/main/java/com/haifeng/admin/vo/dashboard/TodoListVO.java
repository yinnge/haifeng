package com.haifeng.admin.vo.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TodoListVO {
    /** 待处理订单数 */
    private Long pendingOrderCount;
    /** 最新 3 条待处理订单 */
    private List<PendingOrderItem> pendingOrders;

    /** 待处理提现数 */
    private Long pendingWithdrawCount;
    /** 最新 3 条待处理提现 */
    private List<PendingWithdrawItem> pendingWithdraws;

    @Data
    public static class PendingOrderItem {
        private Long id;
        private String orderNo;
        private String memberName;
        private BigDecimal amount;
        private String createdAt;
    }

    @Data
    public static class PendingWithdrawItem {
        private Long id;
        private String memberName;
        private BigDecimal amount;
        private String createdAt;
    }
}
