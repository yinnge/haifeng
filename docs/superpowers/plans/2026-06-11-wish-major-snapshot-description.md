# 为t_wish_major_snapshot添加description字段实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (- [ ]) syntax for tracking.

**Goal:** 为志愿方案专业明细快照表添加description字段，从专业录取明细表获取专业说明并存储

**Architecture:** 在现有addMajors方法中，从AdmissionMajorScore获取description字段值，构建WishMajorSnapshot时赋值，并在分页展示时返回给前端

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL

---

## 文件结构

- haifeng-admin/src/main/resources/db/migration/apps_V18__t_wish_plans_tables.sql - 数据库迁移文件
- haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishMajorSnapshot.java - 实体类
- haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/wish/WishPlanServiceImpl.java - 业务逻辑
- haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanMajorVO.java - 返回VO

---

## Task 1: 修改数据库迁移文件

**Files:**
- Modify: haifeng-admin/src/main/resources/db/migration/apps_V18__t_wish_plans_tables.sql

- [ ] **Step 1: 在t_wish_major_snapshot表添加description字段**

在	uition字段之后，dmission_count字段之前添加：

`sql
description             TEXT,
`

- [ ] **Step 2: 验证SQL语法**

检查SQL文件语法是否正确，确保字段添加在正确位置。

---

## Task 2: 修改实体类

**Files:**
- Modify: haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishMajorSnapshot.java

- [ ] **Step 1: 添加description字段**

在	uition字段之后，dmissionCount字段之前添加：

`java
private String description;
`

- [ ] **Step 2: 验证实体类**

检查实体类注解和字段是否正确。

---

## Task 3: 修改业务逻辑

**Files:**
- Modify: haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/wish/WishPlanServiceImpl.java

- [ ] **Step 1: 修改addMajors方法**

在构建WishMajorSnapshot时，添加description字段赋值：

`java
.description(info.major.getDescription())
`

- [ ] **Step 2: 修改toMajorVO方法**

在toMajorVO方法中，添加description字段映射：

`java
.description(snap.getDescription())
`

- [ ] **Step 3: 验证业务逻辑**

检查addMajors方法和toMajorVO方法是否正确修改。

---

## Task 4: 修改VO类

**Files:**
- Modify: haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanMajorVO.java

- [ ] **Step 1: 添加description字段**

在	uition字段之后，dmissionCount字段之前添加：

`java
private String description;
`

- [ ] **Step 2: 验证VO类**

检查VO类字段是否正确添加。

---

## Task 5: 集成测试

- [ ] **Step 1: 编译项目**

运行Maven编译命令，确保没有编译错误：

`ash
mvn clean compile -DskipTests
`

- [ ] **Step 2: 测试addMajors接口**

使用Postman或curl测试addMajors接口，验证description字段是否正确保存：

`ash
curl -X POST http://localhost:8080/api/v1/app/algorithm/wish-plan/add-majors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"groupId": 1, "majorIds": [1, 2, 3]}'
`

- [ ] **Step 3: 测试分页查询接口**

测试分页查询接口，验证description字段是否正确返回：

`ash
curl -X GET "http://localhost:8080/api/v1/app/algorithm/wish-plan/1/groups?page=1&size=10" \
  -H "Authorization: Bearer <token>"
`

---

## 注意事项

1. 该修改仅影响新添加的数据，已有数据不需要回填
2. description字段为可选字段，允许为NULL
3. 不需要修改WishPlanAddMajorsDTO，因为description从数据库获取
4. 所有修改完成后，需要运行编译和测试验证