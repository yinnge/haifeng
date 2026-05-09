# 考研专业关联管理模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现本科专业与考研方向的多对多关联管理功能，包含 CRUD、分页查询、xlsx 批量导入。

**Architecture:** 基于 Spring Boot 3.x + MyBatis-Plus 的标准分层架构，复用现有 PostgradMajorUniversity 的实现模式。Entity/Mapper 放 haifeng-common，Controller/Service/DTO/VO 放 haifeng-admin/major 子包。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, PostgreSQL, Flyway

---

## Task 1: 创建 Flyway 迁移文件

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V10__create_major_postgrad_direction.sql`

- [ ] **Step 1: 创建迁移文件**

```sql
-- ============================================
-- V10__create_major_postgrad_direction.sql
-- 本科专业-考研方向关联表
-- ============================================

-- ===========================================================
-- 本科专业-考研方向 关联表 (t_major_postgrad_direction)
-- 描述：本科专业 ↔ 考研专业 的多对多推荐关系
-- 关联关系说明（不使用外键约束，性能考虑）：
--   major_id -> t_major.id
--   postgrad_major_id -> t_postgrad_major.id
-- ===========================================================

CREATE TABLE IF NOT EXISTS t_major_postgrad_direction (
    id                      BIGINT          PRIMARY KEY,
    major_id                BIGINT          NOT NULL,
    postgrad_major_id       BIGINT          NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    postgrad_major_name     VARCHAR(100)    NOT NULL,
    sort_order              INTEGER         DEFAULT 0,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_major_postgrad UNIQUE (major_id, postgrad_major_id)
);

-- 索引
CREATE INDEX idx_mpd_major ON t_major_postgrad_direction (major_id);
CREATE INDEX idx_mpd_postgrad ON t_major_postgrad_direction (postgrad_major_id);
CREATE INDEX idx_mpd_major_name ON t_major_postgrad_direction USING btree (major_name varchar_pattern_ops);
CREATE INDEX idx_mpd_postgrad_name ON t_major_postgrad_direction USING btree (postgrad_major_name varchar_pattern_ops);

-- 注释
COMMENT ON TABLE t_major_postgrad_direction IS '本科专业-考研方向关联表：多对多，记录本科专业推荐的考研方向。关联：major_id -> t_major.id, postgrad_major_id -> t_postgrad_major.id';
COMMENT ON COLUMN t_major_postgrad_direction.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_major_postgrad_direction.major_id IS '本科专业ID（关联 t_major.id）';
COMMENT ON COLUMN t_major_postgrad_direction.postgrad_major_id IS '考研专业ID（关联 t_postgrad_major.id）';
COMMENT ON COLUMN t_major_postgrad_direction.major_name IS '本科专业名称（冗余，方便展示）';
COMMENT ON COLUMN t_major_postgrad_direction.postgrad_major_name IS '考研专业名称（冗余，方便展示）';
COMMENT ON COLUMN t_major_postgrad_direction.sort_order IS '推荐排序（数值越小越靠前）';
COMMENT ON COLUMN t_major_postgrad_direction.created_at IS '创建时间';
```

- [ ] **Step 2: 验证迁移文件语法**

Run: `cd haifeng-admin && mvn flyway:validate -Dflyway.validateMigrationNaming=true`

Expected: BUILD SUCCESS 或 flyway 连接相关提示（语法无误即可）

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/resources/db/migration/V10__create_major_postgrad_direction.sql
git commit -m "feat(major): add V10 migration for major_postgrad_direction table"
```

---

## Task 2: 创建 Entity 类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/major/MajorPostgradDirection.java`

- [ ] **Step 1: 创建 Entity**

```java
package com.haifeng.common.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_major_postgrad_direction")
public class MajorPostgradDirection {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long majorId;

    private Long postgradMajorId;

    private String majorName;

    private String postgradMajorName;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/major/MajorPostgradDirection.java
git commit -m "feat(major): add MajorPostgradDirection entity"
```

---

## Task 3: 创建 Mapper 接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorPostgradDirectionMapper.java`
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/PostgradMajorMapper.java`

- [ ] **Step 1: 创建 Mapper 接口**

```java
package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.MajorPostgradDirection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorPostgradDirectionMapper extends BaseMapper<MajorPostgradDirection> {

    @Select("SELECT COUNT(*) > 0 FROM t_major_postgrad_direction WHERE major_id = #{majorId} AND postgrad_major_id = #{postgradMajorId}")
    boolean existsByRelation(@Param("majorId") Long majorId, @Param("postgradMajorId") Long postgradMajorId);
}
```

- [ ] **Step 2: 在 PostgradMajorMapper 中添加按名称查询方法**

在 `PostgradMajorMapper.java` 末尾添加：

```java
    @Select("SELECT id FROM t_postgrad_major WHERE major_name = #{majorName} AND is_deleted = FALSE LIMIT 1")
    Long selectIdByName(@Param("majorName") String majorName);
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorPostgradDirectionMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/major/PostgradMajorMapper.java
git commit -m "feat(major): add MajorPostgradDirectionMapper and PostgradMajorMapper.selectIdByName"
```

---

## Task 4: 创建 DTO 类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorPostgradDirectionQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorPostgradDirectionAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorPostgradDirectionUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorPostgradDirectionImportDTO.java`

- [ ] **Step 1: 创建 QueryDTO**

```java
package com.haifeng.admin.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorPostgradDirectionQueryDTO extends BasePageQueryDTO {

    /**
     * 本科专业名称（模糊查询）
     */
    private String majorName;

    /**
     * 考研专业名称（模糊查询）
     */
    private String postgradMajorName;
}
```

- [ ] **Step 2: 创建 AddDTO**

```java
package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MajorPostgradDirectionAddDTO {

    @NotNull(message = "本科专业ID不能为空")
    private Long majorId;

    @NotNull(message = "考研专业ID不能为空")
    private Long postgradMajorId;

    /**
     * 排序权重，默认0
     */
    private Integer sortOrder;
}
```

- [ ] **Step 3: 创建 UpdateDTO**

```java
package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MajorPostgradDirectionUpdateDTO {

    @NotNull(message = "本科专业ID不能为空")
    private Long majorId;

    @NotNull(message = "考研专业ID不能为空")
    private Long postgradMajorId;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
```

- [ ] **Step 4: 创建 ImportDTO**

```java
package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class MajorPostgradDirectionImportDTO {

    @ExcelProperty("本科专业名称")
    private String majorName;

    @ExcelProperty("考研专业名称")
    private String postgradMajorName;

    @ExcelProperty("排序权重")
    private Integer sortOrder;
}
```

- [ ] **Step 5: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorPostgradDirectionQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorPostgradDirectionAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorPostgradDirectionUpdateDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorPostgradDirectionImportDTO.java
git commit -m "feat(major): add MajorPostgradDirection DTOs"
```

---

## Task 5: 创建 VO 类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/major/MajorPostgradDirectionListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/major/MajorPostgradDirectionDetailVO.java`

- [ ] **Step 1: 创建 ListVO**

```java
package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MajorPostgradDirectionListVO {

    private Long id;

    private String majorName;

    private String postgradMajorName;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: 创建 DetailVO**

```java
package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MajorPostgradDirectionDetailVO {

    private Long id;

    private Long majorId;

    private Long postgradMajorId;

    private String majorName;

    private String postgradMajorName;

    private Integer sortOrder;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/major/MajorPostgradDirectionListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/major/MajorPostgradDirectionDetailVO.java
git commit -m "feat(major): add MajorPostgradDirection VOs"
```

---

## Task 6: 创建 Service 接口

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/major/MajorPostgradDirectionService.java`

- [ ] **Step 1: 创建 Service 接口**

```java
package com.haifeng.admin.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.major.MajorPostgradDirectionAddDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionQueryDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionUpdateDTO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionDetailVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MajorPostgradDirectionService {

    /**
     * 分页查询列表
     */
    IPage<MajorPostgradDirectionListVO> list(MajorPostgradDirectionQueryDTO queryDTO);

    /**
     * 获取详情
     */
    MajorPostgradDirectionDetailVO getDetail(Long id);

    /**
     * 新增关联
     */
    void add(MajorPostgradDirectionAddDTO dto);

    /**
     * 修改关联
     */
    void update(Long id, MajorPostgradDirectionUpdateDTO dto);

    /**
     * 硬删除
     */
    void delete(Long id);

    /**
     * 批量硬删除
     */
    void batchDelete(List<Long> ids);

    /**
     * xlsx批量导入
     */
    ImportResultVO importData(MultipartFile file);
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/major/MajorPostgradDirectionService.java
git commit -m "feat(major): add MajorPostgradDirectionService interface"
```

---

## Task 7: 创建 Service 实现类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/major/MajorPostgradDirectionServiceImpl.java`

- [ ] **Step 1: 创建 Service 实现类**

```java
package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.MajorPostgradDirectionAddDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionImportDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionQueryDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionUpdateDTO;
import com.haifeng.admin.service.major.MajorPostgradDirectionService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionDetailVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionListVO;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.major.MajorPostgradDirection;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorPostgradDirectionServiceImpl implements MajorPostgradDirectionService {

    private final MajorPostgradDirectionMapper majorPostgradDirectionMapper;
    private final MajorMapper majorMapper;
    private final PostgradMajorMapper postgradMajorMapper;

    @Override
    public IPage<MajorPostgradDirectionListVO> list(MajorPostgradDirectionQueryDTO queryDTO) {
        Page<MajorPostgradDirection> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<MajorPostgradDirection> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getMajorName())) {
            wrapper.like(MajorPostgradDirection::getMajorName, queryDTO.getMajorName());
        }
        if (StringUtils.hasText(queryDTO.getPostgradMajorName())) {
            wrapper.like(MajorPostgradDirection::getPostgradMajorName, queryDTO.getPostgradMajorName());
        }

        wrapper.orderByAsc(MajorPostgradDirection::getSortOrder)
               .orderByDesc(MajorPostgradDirection::getCreatedAt);

        Page<MajorPostgradDirection> resultPage = majorPostgradDirectionMapper.selectPage(page, wrapper);

        return resultPage.convert(entity -> {
            MajorPostgradDirectionListVO vo = new MajorPostgradDirectionListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public MajorPostgradDirectionDetailVO getDetail(Long id) {
        MajorPostgradDirection entity = majorPostgradDirectionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        MajorPostgradDirectionDetailVO vo = new MajorPostgradDirectionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MajorPostgradDirectionAddDTO dto) {
        // 查询本科专业
        Major major = majorMapper.selectById(dto.getMajorId());
        if (major == null) {
            throw new BusinessException(400, "本科专业不存在");
        }

        // 查询考研专业名称
        String postgradMajorName = postgradMajorMapper.selectNameByMajorCode(
                postgradMajorMapper.selectById(dto.getPostgradMajorId()) != null ?
                        postgradMajorMapper.selectById(dto.getPostgradMajorId()).getMajorCode() : null);
        if (postgradMajorName == null) {
            throw new BusinessException(400, "考研专业不存在");
        }

        // 检查是否已存在
        if (majorPostgradDirectionMapper.existsByRelation(dto.getMajorId(), dto.getPostgradMajorId())) {
            throw new BusinessException(400, "该关联已存在");
        }

        MajorPostgradDirection entity = MajorPostgradDirection.builder()
                .id(SnowflakeIdGenerator.nextId())
                .majorId(dto.getMajorId())
                .postgradMajorId(dto.getPostgradMajorId())
                .majorName(major.getMajorName())
                .postgradMajorName(postgradMajorName)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .createdAt(OffsetDateTime.now())
                .build();

        majorPostgradDirectionMapper.insert(entity);
        log.info("新增本科专业-考研方向关联成功: majorId={}, postgradMajorId={}", dto.getMajorId(), dto.getPostgradMajorId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MajorPostgradDirectionUpdateDTO dto) {
        MajorPostgradDirection entity = majorPostgradDirectionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        // 如果修改了关联关系，检查新关联是否已存在
        if (!entity.getMajorId().equals(dto.getMajorId()) ||
            !entity.getPostgradMajorId().equals(dto.getPostgradMajorId())) {

            if (majorPostgradDirectionMapper.existsByRelation(dto.getMajorId(), dto.getPostgradMajorId())) {
                throw new BusinessException(400, "该关联已存在");
            }

            // 查询新的名称
            Major major = majorMapper.selectById(dto.getMajorId());
            if (major == null) {
                throw new BusinessException(400, "本科专业不存在");
            }

            String postgradMajorName = postgradMajorMapper.selectNameByMajorCode(
                    postgradMajorMapper.selectById(dto.getPostgradMajorId()) != null ?
                            postgradMajorMapper.selectById(dto.getPostgradMajorId()).getMajorCode() : null);
            if (postgradMajorName == null) {
                throw new BusinessException(400, "考研专业不存在");
            }

            entity.setMajorId(dto.getMajorId());
            entity.setPostgradMajorId(dto.getPostgradMajorId());
            entity.setMajorName(major.getMajorName());
            entity.setPostgradMajorName(postgradMajorName);
        }

        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }

        majorPostgradDirectionMapper.updateById(entity);
        log.info("修改本科专业-考研方向关联成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MajorPostgradDirection entity = majorPostgradDirectionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        majorPostgradDirectionMapper.deleteById(id);
        log.info("删除本科专业-考研方向关联成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        for (Long id : ids) {
            try {
                delete(id);
            } catch (BusinessException e) {
                log.warn("批量删除跳过不存在的关联记录: id={}", id);
            }
        }

        log.info("批量删除本科专业-考研方向关联完成: 请求数量={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<MajorPostgradDirectionImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(MajorPostgradDirectionImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<String> relationKeysInFile = new HashSet<>();
        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            MajorPostgradDirectionImportDTO dto = dataList.get(i);

            // 校验必填字段
            if (!StringUtils.hasText(dto.getMajorName())) {
                errors.add("第" + rowNum + "行: 本科专业名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getPostgradMajorName())) {
                errors.add("第" + rowNum + "行: 考研专业名称不能为空");
                continue;
            }

            // 检查文件内是否有重复组合
            String relationKey = dto.getMajorName() + "|" + dto.getPostgradMajorName();
            if (relationKeysInFile.contains(relationKey)) {
                errors.add("第" + rowNum + "行: [" + dto.getMajorName() + ", " + dto.getPostgradMajorName() + "]组合在文件中重复");
                continue;
            }
            relationKeysInFile.add(relationKey);

            // 根据本科专业名称查询major_id
            Major major = majorMapper.findByMajorName(dto.getMajorName());
            if (major == null) {
                errors.add("第" + rowNum + "行: 本科专业[" + dto.getMajorName() + "]不存在");
                continue;
            }

            // 根据考研专业名称查询postgrad_major_id
            Long postgradMajorId = postgradMajorMapper.selectIdByName(dto.getPostgradMajorName());
            if (postgradMajorId == null) {
                errors.add("第" + rowNum + "行: 考研专业[" + dto.getPostgradMajorName() + "]不存在");
                continue;
            }

            // 检查数据库中是否已存在该关联
            if (majorPostgradDirectionMapper.existsByRelation(major.getId(), postgradMajorId)) {
                errors.add("第" + rowNum + "行: [" + dto.getMajorName() + ", " + dto.getPostgradMajorName() + "]关联已存在");
                continue;
            }

            // 构建实体并插入
            MajorPostgradDirection entity = MajorPostgradDirection.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .majorId(major.getId())
                    .postgradMajorId(postgradMajorId)
                    .majorName(dto.getMajorName())
                    .postgradMajorName(dto.getPostgradMajorName())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .createdAt(now)
                    .build();

            majorPostgradDirectionMapper.insert(entity);
            successCount++;
        }

        if (!errors.isEmpty()) {
            log.warn("导入本科专业-考研方向关联数据部分失败: 成功{}条, 失败{}条", successCount, errors.size());
        } else {
            log.info("导入本科专业-考研方向关联数据成功: 共{}条", successCount);
        }

        return ImportResultVO.builder()
                .total(dataList.size())
                .success(successCount)
                .failed(errors.size())
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/major/MajorPostgradDirectionServiceImpl.java
git commit -m "feat(major): add MajorPostgradDirectionServiceImpl"
```

---

## Task 8: 创建 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/major/MajorPostgradDirectionController.java`

- [ ] **Step 1: 创建 Controller**

```java
package com.haifeng.admin.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.major.MajorPostgradDirectionAddDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionQueryDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionUpdateDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.service.major.MajorPostgradDirectionService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionDetailVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/major-postgrad-direction")
@RequiredArgsConstructor
public class MajorPostgradDirectionController {

    private final MajorPostgradDirectionService majorPostgradDirectionService;

    /**
     * 分页查询本科专业-考研方向关联列表
     */
    @GetMapping("/list")
    public R<IPage<MajorPostgradDirectionListVO>> list(@Valid MajorPostgradDirectionQueryDTO dto) {
        return R.ok(majorPostgradDirectionService.list(dto));
    }

    /**
     * 获取关联详情
     */
    @GetMapping("/{id}")
    public R<MajorPostgradDirectionDetailVO> getDetail(@PathVariable Long id) {
        return R.ok(majorPostgradDirectionService.getDetail(id));
    }

    /**
     * 新增关联
     */
    @PostMapping("/add")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "新增关联")
    public R<Void> add(@Valid @RequestBody MajorPostgradDirectionAddDTO dto) {
        majorPostgradDirectionService.add(dto);
        return R.ok();
    }

    /**
     * 修改关联
     */
    @PutMapping("/{id}")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "修改关联")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MajorPostgradDirectionUpdateDTO dto) {
        majorPostgradDirectionService.update(id, dto);
        return R.ok();
    }

    /**
     * 删除关联（硬删除）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "删除关联")
    public R<Void> delete(@PathVariable Long id) {
        majorPostgradDirectionService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除关联（硬删除）
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "批量删除关联")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        majorPostgradDirectionService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入本科专业-考研方向关联数据
     */
    @PostMapping("/import")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "导入关联数据")
    public R<ImportResultVO> importData(@RequestParam("file") MultipartFile file) {
        return R.ok(majorPostgradDirectionService.importData(file));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/major/MajorPostgradDirectionController.java
git commit -m "feat(major): add MajorPostgradDirectionController"
```

---

## Task 9: 创建接口文档

**Files:**
- Create: `Products/order11.md`

- [ ] **Step 1: 创建接口文档**

```markdown
# 考研专业关联管理模块

## 模块概述

本模块实现本科专业与考研方向的多对多关联管理，属于「专业管理」父模块下的「专业考研关联」子模块。

## 数据表

| 表名 | 说明 |
|------|------|
| t_major_postgrad_direction | 本科专业-考研方向关联表 |

### 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花算法） |
| major_id | BIGINT | 本科专业ID |
| postgrad_major_id | BIGINT | 考研专业ID |
| major_name | VARCHAR(100) | 本科专业名称 |
| postgrad_major_name | VARCHAR(100) | 考研专业名称 |
| sort_order | INTEGER | 推荐排序 |
| created_at | TIMESTAMPTZ | 创建时间 |

## API 接口清单

基础路径：`/api/v1/admin/major-postgrad-direction`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/add` | 新增关联 |
| PUT | `/{id}` | 修改关联 |
| DELETE | `/{id}` | 删除关联（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |
| POST | `/import` | xlsx批量导入 |

### 1. 分页查询列表

**请求**
```
GET /api/v1/admin/major-postgrad-direction/list?page=1&size=10&majorName=计算机
```

**参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| majorName | String | 否 | 本科专业名称（模糊） |
| postgradMajorName | String | 否 | 考研专业名称（模糊） |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1234567890",
        "majorName": "计算机科学与技术",
        "postgradMajorName": "计算机科学与技术",
        "createdAt": "2026-05-09T10:00:00+08:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 2. 获取详情

**请求**
```
GET /api/v1/admin/major-postgrad-direction/{id}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1234567890",
    "majorId": "111",
    "postgradMajorId": "222",
    "majorName": "计算机科学与技术",
    "postgradMajorName": "计算机科学与技术",
    "sortOrder": 0,
    "createdAt": "2026-05-09T10:00:00+08:00"
  }
}
```

### 3. 新增关联

**请求**
```
POST /api/v1/admin/major-postgrad-direction/add
Content-Type: application/json

{
  "majorId": 111,
  "postgradMajorId": 222,
  "sortOrder": 0
}
```

### 4. 修改关联

**请求**
```
PUT /api/v1/admin/major-postgrad-direction/{id}
Content-Type: application/json

{
  "majorId": 111,
  "postgradMajorId": 333,
  "sortOrder": 1
}
```

### 5. 删除关联

**请求**
```
DELETE /api/v1/admin/major-postgrad-direction/{id}
```

### 6. 批量删除

**请求**
```
DELETE /api/v1/admin/major-postgrad-direction/batch
Content-Type: application/json

{
  "ids": [1234567890, 1234567891]
}
```

### 7. xlsx批量导入

**请求**
```
POST /api/v1/admin/major-postgrad-direction/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "success": 95,
    "failed": 5,
    "errors": [
      "第3行: 本科专业[xxx]不存在",
      "第7行: [计算机科学与技术, 软件工程]关联已存在"
    ]
  }
}
```

## xlsx 导入模板

| 本科专业名称 | 考研专业名称 | 排序权重 |
|--------------|--------------|----------|
| 计算机科学与技术 | 计算机科学与技术 | 0 |
| 软件工程 | 软件工程 | 1 |

**字段说明**
| 列名 | 必填 | 说明 |
|------|------|------|
| 本科专业名称 | 是 | 需在t_major表中存在 |
| 考研专业名称 | 是 | 需在t_postgrad_major表中存在 |
| 排序权重 | 否 | 默认0，数值越小越靠前 |

## 实现文件清单

| 类型 | 文件 |
|------|------|
| Entity | `haifeng-common/.../entity/major/MajorPostgradDirection.java` |
| Mapper | `haifeng-common/.../mapper/major/MajorPostgradDirectionMapper.java` |
| Controller | `haifeng-admin/.../controller/major/MajorPostgradDirectionController.java` |
| Service | `haifeng-admin/.../service/major/MajorPostgradDirectionService.java` |
| ServiceImpl | `haifeng-admin/.../service/impl/major/MajorPostgradDirectionServiceImpl.java` |
| QueryDTO | `haifeng-admin/.../dto/major/MajorPostgradDirectionQueryDTO.java` |
| AddDTO | `haifeng-admin/.../dto/major/MajorPostgradDirectionAddDTO.java` |
| UpdateDTO | `haifeng-admin/.../dto/major/MajorPostgradDirectionUpdateDTO.java` |
| ImportDTO | `haifeng-admin/.../dto/major/MajorPostgradDirectionImportDTO.java` |
| ListVO | `haifeng-admin/.../vo/major/MajorPostgradDirectionListVO.java` |
| DetailVO | `haifeng-admin/.../vo/major/MajorPostgradDirectionDetailVO.java` |
| Flyway | `haifeng-admin/.../db/migration/V10__create_major_postgrad_direction.sql` |
```

- [ ] **Step 2: Commit**

```bash
git add Products/order11.md
git commit -m "docs: add order11.md for major-postgrad-direction module"
```

---

## Task 10: 编译验证

**Files:**
- All files created in previous tasks

- [ ] **Step 1: 编译项目**

Run: `mvn clean compile -DskipTests`

Expected: BUILD SUCCESS

- [ ] **Step 2: 检查编译输出**

确认无编译错误，所有类正确导入。

- [ ] **Step 3: Final Commit (如有修复)**

如果编译发现问题并修复，提交修复：

```bash
git add -A
git commit -m "fix(major): resolve compilation issues"
```
