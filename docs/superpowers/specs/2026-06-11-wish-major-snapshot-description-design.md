# 设计文档：为t_wish_major_snapshot添加description字段

## 背景

在志愿方案模块中，用户可以将专业组和专业明细添加到志愿表中。当前	_wish_major_snapshot表缺少专业说明字段，需要从	_admission_major_score表获取description字段并存储到快照表中。

## 需求

1. 在	_wish_major_snapshot表添加description TEXT字段
2. 在添加专业到志愿表时，从	_admission_major_score获取description并存储
3. 在分页展示快照表时，返回description字段给前端

## 设计方案

### 1. 数据库修改

**文件：** pps_V18__t_wish_plans_tables.sql

在	_wish_major_snapshot表中添加description TEXT字段：

`sql
-- 在tuition字段之后，admission_count字段之前添加
description TEXT,
`

### 2. 实体类修改

**文件：** WishMajorSnapshot.java

添加字段：

`java
private String description;
`

### 3. 业务逻辑修改

**文件：** WishPlanServiceImpl.java

#### 3.1 addMajors方法

在构建WishMajorSnapshot时，从AdmissionMajorScore获取description并赋值：

`java
.description(info.major.getDescription())
`

#### 3.2 toMajorVO方法

添加description映射：

`java
.description(snap.getDescription())
`

### 4. VO修改

**文件：** WishPlanMajorVO.java

添加字段：

`java
private String description;
`

## 修改文件清单

1. haifeng-admin/src/main/resources/db/migration/apps_V18__t_wish_plans_tables.sql
   - 在	_wish_major_snapshot表添加description TEXT字段

2. haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishMajorSnapshot.java
   - 添加description字段

3. haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/wish/WishPlanServiceImpl.java
   - 修改ddMajors方法：构建WishMajorSnapshot时赋值description
   - 修改	oMajorVO方法：映射description字段

4. haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanMajorVO.java
   - 添加description字段

## 数据流

1. 用户点击添加专业到志愿表
2. 前端调用ddMajors接口，传入groupId和majorIds
3. 后端从	_admission_major_score获取专业数据（包含description）
4. 构建WishMajorSnapshot时，将description赋值
5. 保存到	_wish_major_snapshot表
6. 用户查看志愿表时，从	_wish_major_snapshot读取description返回给前端

## 注意事项

1. 该修改仅影响新添加的数据，已有数据不需要回填
2. description字段为可选字段，允许为NULL
3. 不需要修改WishPlanAddMajorsDTO，因为description从数据库获取