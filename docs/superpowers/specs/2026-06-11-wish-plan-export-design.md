# 志愿表导出xlsx功能设计文档

## 概述

实现志愿表（t_wish_plan）和快照表（t_wish_group_snapshot、t_wish_major_snapshot）的xlsx导出功能，支持用户自定义排序、导出状态管理、实时进度展示和文件下载。

## 功能需求

### 1. 排序功能

- **专业组排序**：修改 `t_wish_group_snapshot.group_sort_order` 字段
- **专业排序**：修改 `t_wish_major_snapshot.major_sort_order` 字段
- **特点**：直接更新数据库，不用Redis缓存

### 2. 导出状态管理

- **专业明细导出状态**：修改 `t_wish_major_snapshot.is_exported` 字段
- **存储方式**：Redis缓存，key为 `haifeng:wish:export:{planId}`，过期时间7天
- **保存机制**：用户点击"保存"按钮时才同步到数据库
- **专业组全选/取消全选**：点击专业组旁边的按钮，修改该专业组下所有专业的is_exported

### 3. 导出功能

- **权限要求**：Pro或VIP用户（使用 `@RequirePro` 注解）
- **技术实现**：SSE返回进度 + EasyExcel生成文件 + 浏览器下载
- **进度展示**：按专业组个数计算进度（如5个专业组，完成1个返回20%）
- **文件命名**：使用志愿表的 `planName` 字段

## 技术架构

### 模块划分

```
com.haifeng.app/
├── controller/algorithm/wish/
│   └── WishPlanController.java (已有，添加新接口)
├── service/algorithm/wish/
│   ├── WishPlanService.java (已有，添加新方法)
│   └── impl/WishPlanServiceImpl.java (已有，添加新实现)
├── dto/algorithm/wish/
│   ├── WishGroupSortDTO.java (新增)
│   ├── WishMajorSortDTO.java (新增)
│   └── WishMajorExportDTO.java (新增)
├── vo/algorithm/wish/
│   ├── WishPlanExportProgressVO.java (新增)
│   └── WishPlanExportFileVO.java (新增)
├── handler/algorithm/wish/
│   └── WishPlanExcelHandler.java (新增，EasyExcel处理器)
└── util/algorithm/wish/
    └── WishPlanExcelUtil.java (新增，Excel生成工具)
```

### 数据流

1. 前端调用排序接口 → 直接更新数据库
2. 前端调用is_exported接口 → 存Redis缓存
3. 前端调用导出接口 → SSE返回进度 + 文件下载链接

## 接口设计

### 接口列表

| 接口 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 1. 修改专业组排序 | PUT | `/api/v1/app/algorithm/wish-plan/{planId}/groups/sort` | @RequireLogin |
| 2. 修改专业排序 | PUT | `/api/v1/app/algorithm/wish-plan/{planId}/groups/{groupId}/majors/sort` | @RequireLogin |
| 3. 修改专业导出状态 | PUT | `/api/v1/app/algorithm/wish-plan/{planId}/majors/{majorId}/export` | @RequireLogin |
| 4. 批量修改专业组下专业导出状态 | PUT | `/api/v1/app/algorithm/wish-plan/{planId}/groups/{groupId}/export-all` | @RequireLogin |
| 5.1 导出进度SSE | GET | `/api/v1/app/algorithm/wish-plan/{planId}/export/progress` | @RequirePro |
| 5.2 下载导出文件 | GET | `/api/v1/app/algorithm/wish-plan/{planId}/export/download` | @RequirePro |
| 6. 保存is_exported到数据库 | POST | `/api/v1/app/algorithm/wish-plan/{planId}/export/save` | @RequireLogin |

### DTO/VO设计

#### 1. 专业组排序DTO

```java
@Data
public class WishGroupSortDTO {
    @NotEmpty(message = "排序列表不能为空")
    private List<GroupSortItem> items;
    
    @Data
    public static class GroupSortItem {
        @NotNull(message = "专业组ID不能为空")
        private Integer groupId;
        
        @NotNull(message = "排序号不能为空")
        private Integer sortOrder;
    }
}
```

#### 2. 专业排序DTO

```java
@Data
public class WishMajorSortDTO {
    @NotEmpty(message = "排序列表不能为空")
    private List<MajorSortItem> items;
    
    @Data
    public static class MajorSortItem {
        @NotNull(message = "专业ID不能为空")
        private Integer majorId;
        
        @NotNull(message = "排序号不能为空")
        private Integer sortOrder;
    }
}
```

#### 3. 修改专业导出状态DTO

```java
@Data
public class WishMajorExportDTO {
    @NotNull(message = "导出状态不能为空")
    private Boolean isExported;
}
```

#### 4. 批量修改专业组下专业导出状态DTO

```java
@Data
public class WishGroupExportAllDTO {
    @NotNull(message = "导出状态不能为空")
    private Boolean isExported;
}
```

#### 5. SSE进度VO

```java
@Data
@Builder
public class WishPlanExportProgressVO {
    private Integer totalGroups;      // 总专业组数
    private Integer completedGroups;  // 已完成专业组数
    private Integer percentage;       // 进度百分比
    private String status;            // processing/completed/error
    private String message;           // 状态消息（可选）
}
```

#### 6. 下载文件VO

```java
@Data
@Builder
public class WishPlanExportFileVO {
    private String downloadUrl;       // 下载链接
    private String fileName;          // 文件名
}
```

## Redis设计

### Key设计

```
haifeng:wish:export:{planId}  →  Hash
```

### Hash字段

```
major:{majorId}:isExported  →  "true" 或 "false"
```

### 示例

```
Key: haifeng:wish:export:123456
Fields:
  major:1001:isExported → "true"
  major:1002:isExported → "false"
  major:1003:isExported → "true"
```

### 过期时间

7天（604800秒）

### 操作流程

1. **修改is_exported**：`HSET haifeng:wish:export:{planId} major:{majorId}:isExported "true"`
2. **获取所有is_exported**：`HGETALL haifeng:wish:export:{planId}`
3. **保存到数据库**：遍历Hash，更新t_wish_major_snapshot表，然后删除Key
4. **导出时**：读取Redis中的is_exported状态，过滤要导出的专业

### 边界情况处理

- 如果Redis中没有某个专业的is_exported记录，使用数据库中的默认值（true）
- 如果用户从未修改过is_exported，Redis中没有该planId的key，导出所有is_exported=true的专业

## EasyExcel模板设计

### 表结构

| 行号 | 内容 | 样式 |
|------|------|------|
| 第1行 | 【planName】【planYear】【planProvince】【planBatch】【reformModel】 userScore分/userRank名 导出时间 | 宋体16号，居中，绿色背景，合并单元格 |
| 第2行 | 表头：组号、大学信息、院校组代码、院校组名称、描述、专业数量、推免年份、推免率、序号、专业名称、学费/学制、年份、计划招生人数、最低分、最低位次、平均分、平均位次、最高分、最高位次 | 宋体13号，居中，绿色背景 |
| 第3-7行 | 5年数据（倒序），其他列合并 | 宋体12号，居中，动态列宽 |

### 第1行详细格式

```
【planName】【planYear】【planProvince】【planBatch】【reformModel】 userScore分/userRank名 导出时间
```

示例：
```
【我的志愿方案1】【2020】【广东】【本科批】【理科】 471分/78039名 2025-06-27 15:28:27
```

### 第2行表头字段

| 列号 | 字段名 | 数据来源 |
|------|--------|----------|
| A | 组号 | groupSortOrder |
| B | 大学信息 | universityName + cityName + category + nature + tags |
| C | 院校组代码 | groupCode |
| D | 院校组名称 | groupName + enrollmentCode + subjects |
| E | 描述 | description + constraintsDescription |
| F | 专业数量 | majorCount |
| G | 推免年份 | recommendationYear |
| H | 推免率 | recommendationRate |
| I | 序号 | majorSortOrder |
| J | 专业名称 | majorName + majorCode + description |
| K | 学费/学制 | duration + tuition |
| L | 年份1 | historyScores[0].year |
| M | 年份2 | historyScores[1].year |
| N | 年份3 | historyScores[2].year |
| O | 年份4 | historyScores[3].year |
| P | 年份5 | historyScores[4].year |
| Q | 计划招生人数1 | historyScores[0].admissionCount |
| R | 计划招生人数2 | historyScores[1].admissionCount |
| S | 计划招生人数3 | historyScores[2].admissionCount |
| T | 计划招生人数4 | historyScores[3].admissionCount |
| U | 计划招生人数5 | historyScores[4].admissionCount |
| V | 最低分1 | historyScores[0].minScore |
| W | 最低分2 | historyScores[1].minScore |
| X | 最低分3 | historyScores[2].minScore |
| Y | 最低分4 | historyScores[3].minScore |
| Z | 最低分5 | historyScores[4].minScore |
| AA | 最低位次1 | historyScores[0].minRank |
| AB | 最低位次2 | historyScores[1].minRank |
| AC | 最低位次3 | historyScores[2].minRank |
| AD | 最低位次4 | historyScores[3].minRank |
| AE | 最低位次5 | historyScores[4].minRank |
| AF | 平均分1 | historyScores[0].avgScore |
| AG | 平均分2 | historyScores[1].avgScore |
| AH | 平均分3 | historyScores[2].avgScore |
| AI | 平均分4 | historyScores[3].avgScore |
| AJ | 平均分5 | historyScores[4].avgScore |
| AK | 平均位次1 | historyScores[0].avgRank |
| AL | 平均位次2 | historyScores[1].avgRank |
| AM | 平均位次3 | historyScores[2].avgRank |
| AN | 平均位次4 | historyScores[3].avgRank |
| AO | 平均位次5 | historyScores[4].avgRank |
| AP | 最高分1 | historyScores[0].maxScore |
| AQ | 最高分2 | historyScores[1].maxScore |
| AR | 最高分3 | historyScores[2].maxScore |
| AS | 最高分4 | historyScores[3].maxScore |
| AT | 最高分5 | historyScores[4].maxScore |
| AU | 最高位次1 | historyScores[0].maxRank |
| AV | 最高位次2 | historyScores[1].maxRank |
| AW | 最高位次3 | historyScores[2].maxRank |
| AX | 最高位次4 | historyScores[3].maxRank |
| AY | 最高位次5 | historyScores[4].maxRank |

### 合并单元格规则

1. **第1行**：所有列合并为一个单元格
2. **第2行**：专业组信息部分（A-H列），如果连续多个专业属于同一专业组，合并这些列
3. **第3-7行**：
   - 年份数据部分（L-AY列）：每个专业占5行（5年数据），每个年份字段占1列
   - 其他列（A-K列）：每个专业合并5行
   - 如果连续多个专业属于同一专业组，专业组信息列（A-H列）合并

### 年份数据倒序规则

historyScores数组按年份从新到旧排列：
- 第3行：最新年份数据
- 第4行：次新年份数据
- ...
- 第7行：最旧年份数据

## 业务流程

### 1. 用户修改排序

```
前端 → PUT /groups/sort 或 /majors/sort
     → 更新数据库 group_sort_order 或 major_sort_order
     → 返回成功
```

### 2. 用户修改专业导出状态

```
前端 → PUT /majors/{majorId}/export
     → HSET haifeng:wish:export:{planId} major:{majorId}:isExported "true"
     → 返回成功
```

### 3. 用户全选/取消全选专业组

```
前端 → PUT /groups/{groupId}/export-all
     → 获取该专业组下所有专业
     → 批量HSET haifeng:wish:export:{planId} major:{majorId}:isExported "true"
     → 返回成功
```

### 4. 用户导出xlsx

```
前端 → GET /export/progress (SSE连接)
     → 后端开始生成xlsx
     → 每完成一个专业组，发送进度事件
     → 完成后发送completed事件
     → 前端 → GET /export/download
     → 返回xlsx文件下载
```

### 5. 用户保存导出状态到数据库

```
前端 → POST /export/save
     → HGETALL haifeng:wish:export:{planId}
     → 批量UPDATE t_wish_major_snapshot SET is_exported = ?
     → DEL haifeng:wish:export:{planId}
     → 返回成功
```

## 错误处理

### 1. 权限错误

- 未登录：返回401，提示"请先登录"
- 非Pro/VIP用户：返回403，提示"需要Pro或VIP权限才能导出"

### 2. 数据错误

- 志愿表不存在：返回404，提示"志愿表不存在"
- 专业组不存在：返回404，提示"专业组不存在"
- 专业不存在：返回404，提示"专业不存在"

### 3. 导出错误

- SSE连接失败：返回500，提示"导出服务暂时不可用"
- Excel生成失败：返回500，提示"导出失败，请稍后重试"

## 性能优化

### 1. Redis操作优化

- 使用pipeline批量操作Redis
- 导出时一次性读取所有is_exported状态

### 2. Excel生成优化

- 使用EasyExcel的流式写入，减少内存占用
- 大文件分片生成（如果专业组数量过多）

### 3. 数据库查询优化

- 使用批量查询，减少数据库往返次数
- 添加必要的索引（plan_id, group_sort_order, major_sort_order）

## 依赖项

### 1. EasyExcel

需要在pom.xml中添加依赖：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>3.3.4</version>
</dependency>
```

### 2. Redis

已有Redis配置，无需额外配置。

## 测试要点

### 1. 功能测试

- 排序接口：验证数据库更新正确
- is_exported接口：验证Redis存储正确
- 导出接口：验证xlsx文件生成正确
- 保存接口：验证数据库同步正确

### 2. 边界测试

- 空数据导出
- 大量数据导出（性能测试）
- 并发导出测试

### 3. 权限测试

- 未登录访问导出接口
- 非Pro/VIP用户访问导出接口

## 实现计划

### 阶段1：基础功能（1-2天）

1. 添加EasyExcel依赖到pom.xml
2. 实现排序接口
3. 实现is_exported接口
4. 实现Redis操作

### 阶段2：导出功能（2-3天）

1. 实现Excel模板和处理器
2. 实现导出进度SSE
3. 实现文件下载接口
4. 实现保存接口

### 阶段3：测试和优化（1-2天）

1. 功能测试
2. 性能优化
3. 错误处理完善
