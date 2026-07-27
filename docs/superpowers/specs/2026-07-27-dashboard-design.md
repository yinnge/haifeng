# 控制面板（Dashboard）设计文档

## 1. 需求概述

为后台管理系统新增控制面板模块，展示关键业务统计数据和图表。

### 1.1 功能需求

| 功能 | 说明 |
|------|------|
| 会员统计 | 总会员数、Pro会员数、VIP会员数 |
| 订单统计 | 待处理订单数、累计总金额 |
| 实体统计 | 大学、专业、行业、企业、AdmissionGroup、AdmissionMajorScore数量 |
| 图表展示 | 柱状图+折线图混合图表展示实体数据 |
| 权限控制 | 仅管理员可访问 |

### 1.2 技术决策

| 项目 | 决定 |
|------|------|
| 布局 | 卡片式（顶部统计卡片 + 下方图表） |
| 图表 | 混合图表（柱状图+折线图） |
| 接口设计 | 单一接口返回所有数据 |
| 权限 | @RequireAdminModule("dashboard") |

---

## 2. 数据库变更

### 2.1 member_orders 表新增 status 字段

```sql
ALTER TABLE member_orders 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'pending';

ALTER TABLE member_orders 
ADD CONSTRAINT chk_member_orders_status 
CHECK (status IN ('pending', 'completed', 'cancelled'));

CREATE INDEX idx_member_orders_status ON member_orders(status) WHERE is_deleted = FALSE;

COMMENT ON COLUMN member_orders.status IS '订单状态: pending-待处理, completed-已完成, cancelled-已取消';
```

### 2.2 system_settings 表新增 total_amount 字段

```sql
ALTER TABLE system_settings 
ADD COLUMN total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;

COMMENT ON COLUMN system_settings.total_amount IS '累计订单总金额（仅已完成订单）';
```

### 2.3 新增控制面板模块

```sql
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order) 
VALUES (2074728249027596287, '控制面板', 'dashboard', NULL, 1, '/dashboard', 0)
ON CONFLICT (module_code) DO NOTHING;

INSERT INTO sys_role_module (id, role_id, module_id) 
VALUES (2074728249031790591, 2074728248943710208, 2074728249027596287)
ON CONFLICT (role_id, module_id) DO NOTHING;
```

---

## 3. 后端模块结构

### 3.1 目录结构

```
com.haifeng.admin/
├── controller/dashboard/
│   └── DashboardController.java
├── service/dashboard/
│   └── DashboardService.java
├── service/impl/dashboard/
│   └── DashboardServiceImpl.java
└── vo/dashboard/
    └── DashboardStatsVO.java
```

### 3.2 新增枚举

```
com.haifeng.common.enums.OrderStatus
```

### 3.3 实体修改

- MemberOrder: 新增 status 字段
- SystemSettings: 新增 totalAmount 字段

---

## 4. 接口设计

### 4.1 GET /api/v1/admin/dashboard/stats

**权限**: @RequireAdminModule("dashboard")

**响应结构**:

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "memberStats": {
      "totalMembers": 1000,
      "proMembers": 200,
      "vipMembers": 150
    },
    "orderStats": {
      "pendingOrders": 10,
      "totalAmount": 50000.00
    },
    "entityStats": {
      "universityCount": 500,
      "majorCount": 800,
      "industryCount": 50,
      "enterpriseCount": 300,
      "admissionGroupCount": 2000,
      "admissionMajorScoreCount": 15000
    }
  }
}
```

---

## 5. 实现步骤

1. 创建 V33 数据库迁移文件
2. 新增 OrderStatus 枚举
3. 修改 MemberOrder 实体
4. 修改 SystemSettings 实体
5. 创建 DashboardStatsVO
6. 创建 DashboardService 接口
7. 创建 DashboardServiceImpl 实现
8. 创建 DashboardController
