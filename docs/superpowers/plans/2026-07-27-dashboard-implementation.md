# 控制面板（Dashboard）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为后台管理系统新增控制面板模块，展示关键业务统计数据和图表。

**Architecture:** 单一接口返回所有统计数据，包含会员统计、订单统计和实体统计。使用MyBatis-Plus进行数据库查询，通过@RequireAdminModule注解控制权限。

**Tech Stack:** Spring Boot 3.x + MyBatis-Plus + PostgreSQL

---

## 文件结构

```
haifeng-admin/src/main/resources/db/migration/
└── V33__add_dashboard_and_order_status.sql

haifeng-common/src/main/java/com/haifeng/common/
├── enums/OrderStatus.java
├── entity/user/MemberOrder.java (修改)
└── entity/system/SystemSettings.java (修改)

haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/dashboard/DashboardController.java
├── service/dashboard/DashboardService.java
├── service/impl/dashboard/DashboardServiceImpl.java
└── vo/dashboard/DashboardStatsVO.java
```

---

### Task 1: 创建数据库迁移文件

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V33__add_dashboard_and_order_status.sql`

- [ ] **Step 1: 创建V33迁移文件**

```sql
-- V33__add_dashboard_and_order_status.sql
-- 1. member_orders 新增 status 字段
ALTER TABLE member_orders 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'pending';

ALTER TABLE member_orders 
ADD CONSTRAINT chk_member_orders_status 
CHECK (status IN ('pending', 'completed', 'cancelled'));

CREATE INDEX idx_member_orders_status ON member_orders(status) WHERE is_deleted = FALSE;

COMMENT ON COLUMN member_orders.status IS '订单状态: pending-待处理, completed-已完成, cancelled-已取消';

-- 2. system_settings 新增 total_amount 字段
ALTER TABLE system_settings 
ADD COLUMN total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;

COMMENT ON COLUMN system_settings.total_amount IS '累计订单总金额（仅已完成订单）';

-- 3. 新增控制面板模块（1级目录，sort_order=0，排在最前面）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order) 
VALUES (2074728249027596287, '控制面板', 'dashboard', NULL, 1, '/dashboard', 0)
ON CONFLICT (module_code) DO NOTHING;

-- 4. 超级管理员绑定控制面板模块
INSERT INTO sys_role_module (id, role_id, module_id) 
VALUES (2074728249031790591, 2074728248943710208, 2074728249027596287)
ON CONFLICT (role_id, module_id) DO NOTHING;
```

- [ ] **Step 2: 提交代码**

```bash
git add haifeng-admin/src/main/resources/db/migration/V33__add_dashboard_and_order_status.sql
git commit -m "feat: 添加控制面板数据库迁移文件"
```

---

### Task 2: 新增OrderStatus枚举

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/OrderStatus.java`

- [ ] **Step 1: 创建OrderStatus枚举**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OrderStatus {

    PENDING("pending", "待处理"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    @EnumValue
    private final String value;
    private final String desc;

    OrderStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
```

- [ ] **Step 2: 提交代码**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/enums/OrderStatus.java
git commit -m "feat: 添加OrderStatus枚举"
```

---

### Task 3: 修改MemberOrder实体

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/entity/user/MemberOrder.java`

- [ ] **Step 1: 添加status字段**

在MemberOrder.java中添加以下字段：

```java
private OrderStatus status;
```

并在文件顶部添加导入：

```java
import com.haifeng.common.enums.OrderStatus;
```

- [ ] **Step 2: 提交代码**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/user/MemberOrder.java
git commit -m "feat: MemberOrder实体添加status字段"
```

---

### Task 4: 修改SystemSettings实体

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/entity/system/SystemSettings.java`

- [ ] **Step 1: 添加totalAmount字段**

在SystemSettings.java中添加以下字段：

```java
private BigDecimal totalAmount;
```

并在文件顶部添加导入：

```java
import java.math.BigDecimal;
```

- [ ] **Step 2: 提交代码**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/system/SystemSettings.java
git commit -m "feat: SystemSettings实体添加totalAmount字段"
```

---

### Task 5: 创建DashboardStatsVO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/dashboard/DashboardStatsVO.java`

- [ ] **Step 1: 创建DashboardStatsVO**

```java
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
```

- [ ] **Step 2: 提交代码**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/dashboard/DashboardStatsVO.java
git commit -m "feat: 创建DashboardStatsVO"
```

---

### Task 6: 创建DashboardService接口

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/dashboard/DashboardService.java`

- [ ] **Step 1: 创建DashboardService接口**

```java
package com.haifeng.admin.service.dashboard;

import com.haifeng.admin.vo.dashboard.DashboardStatsVO;

public interface DashboardService {
    DashboardStatsVO getDashboardStats();
}
```

- [ ] **Step 2: 提交代码**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/dashboard/DashboardService.java
git commit -m "feat: 创建DashboardService接口"
```

---

### Task 7: 创建DashboardServiceImpl实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/dashboard/DashboardServiceImpl.java`

- [ ] **Step 1: 创建DashboardServiceImpl**

```java
package com.haifeng.admin.service.impl.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.service.dashboard.DashboardService;
import com.haifeng.admin.vo.dashboard.DashboardStatsVO;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.industry.Industry;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.enums.OrderStatus;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberMapper memberMapper;
    private final MemberOrderMapper memberOrderMapper;
    private final UniversityMapper universityMapper;
    private final MajorMapper majorMapper;
    private final IndustryMapper industryMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final AdmissionGroupMapper admissionGroupMapper;
    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final SystemSettingsMapper systemSettingsMapper;

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setMemberStats(getMemberStats());
        vo.setOrderStats(getOrderStats());
        vo.setEntityStats(getEntityStats());
        return vo;
    }

    private DashboardStatsVO.MemberStats getMemberStats() {
        DashboardStatsVO.MemberStats stats = new DashboardStatsVO.MemberStats();
        
        stats.setTotalMembers(memberMapper.selectCount(
            new LambdaQueryWrapper<Member>().eq(Member::getDeleted, false)));
        
        stats.setProMembers(memberMapper.selectCount(
            new LambdaQueryWrapper<Member>()
                .eq(Member::getDeleted, false)
                .eq(Member::getMemberType, "pro")));
        
        stats.setVipMembers(memberMapper.selectCount(
            new LambdaQueryWrapper<Member>()
                .eq(Member::getDeleted, false)
                .eq(Member::getMemberType, "vip")
                .gt(Member::getExpireAt, OffsetDateTime.now())));
        
        return stats;
    }

    private DashboardStatsVO.OrderStats getOrderStats() {
        DashboardStatsVO.OrderStats stats = new DashboardStatsVO.OrderStats();
        
        stats.setPendingOrders(memberOrderMapper.selectCount(
            new LambdaQueryWrapper<MemberOrder>()
                .eq(MemberOrder::getDeleted, false)
                .eq(MemberOrder::getStatus, OrderStatus.PENDING)));
        
        SystemSettings settings = systemSettingsMapper.selectById(1L);
        stats.setTotalAmount(settings != null ? settings.getTotalAmount() : BigDecimal.ZERO);
        
        return stats;
    }

    private DashboardStatsVO.EntityStats getEntityStats() {
        DashboardStatsVO.EntityStats stats = new DashboardStatsVO.EntityStats();
        
        stats.setUniversityCount(universityMapper.selectCount(null));
        stats.setMajorCount(majorMapper.selectCount(null));
        stats.setIndustryCount(industryMapper.selectCount(null));
        stats.setEnterpriseCount(enterpriseMapper.selectCount(null));
        stats.setAdmissionGroupCount(admissionGroupMapper.selectCount(null));
        stats.setAdmissionMajorScoreCount(admissionMajorScoreMapper.selectCount(null));
        
        return stats;
    }
}
```

- [ ] **Step 2: 提交代码**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/dashboard/DashboardServiceImpl.java
git commit -m "feat: 创建DashboardServiceImpl实现"
```

---

### Task 8: 创建DashboardController

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/dashboard/DashboardController.java`

- [ ] **Step 1: 创建DashboardController**

```java
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
```

- [ ] **Step 2: 提交代码**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/dashboard/DashboardController.java
git commit -m "feat: 创建DashboardController"
```

---

### Task 9: 编译验证

**Files:**
- None (验证任务)

- [ ] **Step 1: 编译项目**

```bash
cd haifeng-admin && mvn compile
```

- [ ] **Step 2: 提交最终代码**

```bash
git add -A
git commit -m "feat: 完成控制面板模块开发"
```
