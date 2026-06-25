# 志愿方案模块设计文档

## 概述

实现高考志愿填报核心功能：用户从专业明细分页勾选专业，添加到志愿表中，并支持查看志愿表详情（志愿组快照/专业快照）。

## 角色与权限

| 角色 | 最大志愿表数 | 权限要求 |
|------|------------|---------|
| normal | 1 | `@RequireLogin` |
| pro | 5 | `@RequireLogin` |
| vip | 10 | `@RequireLogin` |

## API 设计

### 1. 添加专业明细到志愿表

```
POST /api/v1/app/algorithm/wish-plan/add-majors
@RequireLogin
```

**Request Body:**
```json
{
  "groupId": 123,
  "majorIds": [1, 2, 3]
}
```

**校验规则（按顺序）：**
1. 当前登录用户 → 获取 memberId
2. 不允许添加 `levelShort == "禁"` 的专业
3. 用户已有志愿表数 < memberType 上限 → 否则返回 400
4. 已有草稿plan → 复用；无 → 从 `t_member_gaokao` 创建新plan
5. 新增专业各档位数 + plan已选档位数 ≤ system_settings 对应上限
6. 通过 → 写入 snapshot，更新 plan 计数

**Plan 命名算法：** `我的志愿方案{该用户已有plan数 + 1}`

**Plan 初始化数据来源：** 从 `t_member_gaokao` 读取 member_id 对应的 `gaokao_year`, `gaokao_province`, `reform_model`, `score`, `rank`, `batch`

### 2. 查看我的志愿表列表

```
GET /api/v1/app/algorithm/wish-plan/my-plans
@RequireLogin
```

返回该 member 所有未被软删除的 WishPlan。

### 3. 删除志愿表

```
DELETE /api/v1/app/algorithm/wish-plan/{planId}
@RequireLogin
```

级联删除：
- 软删除 `t_wish_plan`（`is_deleted = true`）
- 硬删除关联的 `t_wish_group_snapshot` 和 `t_wish_major_snapshot`

### 4. 分页查志愿组快照

```
GET /api/v1/app/algorithm/wish-plan/{planId}/groups
@RequireLogin
```

返回该 plan 下所有 group snapshots（分页）。

### 5. 分页查专业快照

```
GET /api/v1/app/algorithm/wish-plan/{planId}/groups/{groupSnapshotId}/majors
@RequireLogin
```

返回该 group snapshot 下所有 major snapshots（分页）。

## 新增/修改文件清单

### haifeng-app

| 文件 | 类型 | 说明 |
|------|------|------|
| `dto/algorithm/wish/WishPlanAddMajorsDTO.java` | 新增 | 添加专业明细请求体 |
| `vo/algorithm/wish/WishPlanListVO.java` | 新增 | 志愿表列表VO |
| `vo/algorithm/wish/WishPlanGroupVO.java` | 新增 | 志愿组快照VO |
| `vo/algorithm/wish/WishPlanMajorVO.java` | 新增 | 志愿专业快照VO |
| `controller/algorithm/WishPlanController.java` | 修改 | 追加5个新接口 |
| `service/algorithm/wish/WishPlanService.java` | 修改 | 追加5个新方法 |
| `service/impl/algorithm/wish/WishPlanServiceImpl.java` | 修改 | 追加实现 |

### haifeng-common

| 文件 | 类型 | 说明 |
|------|------|------|
| `entity/algorithm/wish/WishGroupSnapshot.java` | 修改 | 补充 category, majorCount, nature, tags 字段 |
| `mapper/university/UniversityMapper.java` | 已有 | 无需修改，已有 `selectById` |
| `mapper/algorithm/MemberGaokaoMapper.java` | 已有 | 无需修改，已有 `selectByMemberId` |

## 核心逻辑：add-majors

```
1. memberId = SecurityUtil.getCurrentMemberId()
2. 查询 majorIds 对应的 t_admission_major_score 数据
   → 获取每个 major 的 safety_level / level_short / history_scores
3. 校验无 "禁" 级别
4. 校验用户 plan 数量未达上限
5. 获取/创建 WishPlan
6. 校验档位数量：
   - 获取 system_settings 限制
   - 统计 new majors 各档位数 + plan 已有数 ≤ 限制
7. 创建/复用 WishGroupSnapshot：
   - 按 planId + groupId 去重
   - 补充 university 信息 (category, nature, tags)
8. 批量创建 WishMajorSnapshot
9. 更新 WishPlan 的 bo~die_limit 计数
```

## 关键约束

- 后端对"禁"级别做强制校验（即使前端已过滤）
- 档位数量校验："可以少或等于，不能多"
- 所有操作在事务中执行
- Plan 的 bo_limit ~ die_limit 存储当前已选数量（非上限）
