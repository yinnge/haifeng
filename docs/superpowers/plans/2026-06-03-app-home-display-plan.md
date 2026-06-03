# App 端首页展示模块实现计划（公告 / 规划师 / 培训机构）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 C 端实现公告 / 规划师 / 培训机构 6 个公开只读接口（3 列表 + 3 详情），叠加 Redis 30 分钟 TTL 被动过期缓存。

**Architecture:** 三模块平行结构。每个模块独立 Controller + Service + ServiceImpl，复用 `haifeng-common` 已有的 Entity 和 Mapper（`Announcement`/`Planner`/`Institution`）。缓存读写在 ServiceImpl 内统一套路：构造 key → 查 Redis → 命中直返 / 未命中查 DB 后回写。引入通用 `PageCacheDTO<T>` 作为分页结果的序列化中转结构，绕开 MyBatis-Plus `Page` 的 Jackson 序列化坑。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, Spring Data Redis（JSON 序列化已就绪）, JUnit 5 + Mockito（`spring-boot-starter-test`）。

**Spec:** `docs/superpowers/specs/2026-06-03-app-home-display-design.md`

---

## 文件结构

### 新建文件（19 个）

| 路径 | 职责 |
|------|------|
| `haifeng-common/.../dto/common/PageCacheDTO.java` | 通用分页缓存中转结构（records/total/current/size） |
| `haifeng-app/.../controller/home/AnnouncementController.java` | 公告 GET 列表 / 详情 |
| `haifeng-app/.../controller/home/PlannerController.java` | 规划师 GET 列表 / 详情 |
| `haifeng-app/.../controller/home/InstitutionController.java` | 培训机构 GET 列表 / 详情 |
| `haifeng-app/.../service/home/AnnouncementService.java` | 公告服务接口 |
| `haifeng-app/.../service/home/PlannerService.java` | 规划师服务接口 |
| `haifeng-app/.../service/home/InstitutionService.java` | 培训机构服务接口 |
| `haifeng-app/.../service/impl/home/AnnouncementServiceImpl.java` | 公告服务实现 + 缓存 |
| `haifeng-app/.../service/impl/home/PlannerServiceImpl.java` | 规划师服务实现 + region 校验 + 缓存 |
| `haifeng-app/.../service/impl/home/InstitutionServiceImpl.java` | 培训机构服务实现 + 缓存 |
| `haifeng-app/.../dto/home/AnnouncementQueryDTO.java` | 含 tag 精准筛选 |
| `haifeng-app/.../dto/home/PlannerQueryDTO.java` | 含 region 精准筛选 |
| `haifeng-app/.../dto/home/InstitutionQueryDTO.java` | 仅继承分页基类 |
| `haifeng-app/.../vo/home/AnnouncementListVO.java` | id, title, tag, updatedAt |
| `haifeng-app/.../vo/home/AnnouncementDetailVO.java` | id, title, content, tag |
| `haifeng-app/.../vo/home/PlannerListVO.java` | id, name, region, position, avatar, specialty, personalDescription |
| `haifeng-app/.../vo/home/PlannerDetailVO.java` | id + 全部展示字段 |
| `haifeng-app/.../vo/home/InstitutionListVO.java` | id, name, type, description, images |
| `haifeng-app/.../vo/home/InstitutionDetailVO.java` | id, name, type, phone, address, description, courses, images, logo |
| `haifeng-app/src/test/java/com/haifeng/app/service/home/AnnouncementServiceImplTest.java` | 单测 |
| `haifeng-app/src/test/java/com/haifeng/app/service/home/PlannerServiceImplTest.java` | 单测（含 region 校验） |
| `haifeng-app/src/test/java/com/haifeng/app/service/home/InstitutionServiceImplTest.java` | 单测 |

### 修改文件（1 个）

| 路径 | 修改内容 |
|------|----------|
| `haifeng-common/.../constant/RedisKeyConstant.java` | 追加 6 个 key 前缀、TTL 常量、6 个 key 构造方法 |

### 模块实现顺序

1. **基础设施**（Task 1-2）：`PageCacheDTO` + `RedisKeyConstant`
2. **公告模块**（Task 3-7）：VO/DTO → Service 接口 → 单测 → 实现 → Controller
3. **规划师模块**（Task 8-12）：含 region 校验，测试要覆盖
4. **培训机构模块**（Task 13-17）
5. **手动验证**（Task 18）

---

### Task 1: 新建 `PageCacheDTO<T>` 通用结构

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/dto/common/PageCacheDTO.java`

- [ ] **Step 1: 创建文件**

```java
package com.haifeng.common.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果缓存中转结构
 *
 * <p>MyBatis-Plus 的 {@link com.baomidou.mybatisplus.extension.plugins.pagination.Page}
 * 通过 Jackson 序列化进 Redis 时容易丢失类型，借助本类做一次扁平化转换。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageCacheDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private long current;
    private long size;
}
```

- [ ] **Step 2: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-common -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/dto/common/PageCacheDTO.java
git commit -m "feat(common): add PageCacheDTO for Redis-friendly page serialization

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 2: 扩展 `RedisKeyConstant` 加入首页缓存 key

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java`

- [ ] **Step 1: 在文件末尾的 `}` 之前追加常量与构造方法**

把以下代码插入到 `RedisKeyConstant.java` 类的末尾（最后一个 `}` 之前）：

```java
    /**
     * 首页 - 公告缓存
     */
    public static final String HOME_ANNOUNCEMENT_LIST_PREFIX   = "haifeng:app:home:announcement:list:";
    public static final String HOME_ANNOUNCEMENT_DETAIL_PREFIX = "haifeng:app:home:announcement:detail:";

    /**
     * 首页 - 规划师缓存
     */
    public static final String HOME_PLANNER_LIST_PREFIX   = "haifeng:app:home:planner:list:";
    public static final String HOME_PLANNER_DETAIL_PREFIX = "haifeng:app:home:planner:detail:";

    /**
     * 首页 - 培训机构缓存
     */
    public static final String HOME_INSTITUTION_LIST_PREFIX   = "haifeng:app:home:institution:list:";
    public static final String HOME_INSTITUTION_DETAIL_PREFIX = "haifeng:app:home:institution:detail:";

    /**
     * 首页模块缓存 TTL（分钟）
     */
    public static final long HOME_CACHE_TTL_MINUTES = 30L;

    /**
     * 公告列表缓存 Key
     */
    public static String getAnnouncementListKey(int page, int size, String tag) {
        return HOME_ANNOUNCEMENT_LIST_PREFIX + "p=" + page + ":s=" + size + ":tag=" + (tag == null ? "" : tag);
    }

    /**
     * 公告详情缓存 Key
     */
    public static String getAnnouncementDetailKey(Long id) {
        return HOME_ANNOUNCEMENT_DETAIL_PREFIX + id;
    }

    /**
     * 规划师列表缓存 Key
     */
    public static String getPlannerListKey(int page, int size, String region) {
        return HOME_PLANNER_LIST_PREFIX + "p=" + page + ":s=" + size + ":region=" + (region == null ? "" : region);
    }

    /**
     * 规划师详情缓存 Key
     */
    public static String getPlannerDetailKey(Long id) {
        return HOME_PLANNER_DETAIL_PREFIX + id;
    }

    /**
     * 培训机构列表缓存 Key
     */
    public static String getInstitutionListKey(int page, int size) {
        return HOME_INSTITUTION_LIST_PREFIX + "p=" + page + ":s=" + size;
    }

    /**
     * 培训机构详情缓存 Key
     */
    public static String getInstitutionDetailKey(Long id) {
        return HOME_INSTITUTION_DETAIL_PREFIX + id;
    }
```

- [ ] **Step 2: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-common -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java
git commit -m "feat(common): add home module redis cache keys and TTL constant

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 3: 公告 VO（List + Detail）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/home/AnnouncementListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/home/AnnouncementDetailVO.java`

- [ ] **Step 1: 新建 `AnnouncementListVO`**

```java
package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class AnnouncementListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String tag;
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 新建 `AnnouncementDetailVO`**

```java
package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;

@Data
public class AnnouncementDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    private String tag;
}
```

- [ ] **Step 3: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/home/
git commit -m "feat(app/home): add AnnouncementListVO and AnnouncementDetailVO

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 4: 公告 QueryDTO + Service 接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/home/AnnouncementQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/home/AnnouncementService.java`

- [ ] **Step 1: 新建 `AnnouncementQueryDTO`**

```java
package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementQueryDTO extends BasePageQueryDTO {

    /** 标签精准匹配（可选） */
    private String tag;
}
```

- [ ] **Step 2: 新建 `AnnouncementService` 接口**

```java
package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.AnnouncementQueryDTO;
import com.haifeng.app.vo.home.AnnouncementDetailVO;
import com.haifeng.app.vo.home.AnnouncementListVO;

public interface AnnouncementService {

    /** 分页查询展示中的公告（status=1） */
    IPage<AnnouncementListVO> page(AnnouncementQueryDTO dto);

    /** 查询公告详情（仅 status=1，不存在抛 404） */
    AnnouncementDetailVO detail(Long id);
}
```

- [ ] **Step 3: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/home/AnnouncementQueryDTO.java haifeng-app/src/main/java/com/haifeng/app/service/home/AnnouncementService.java
git commit -m "feat(app/home): add AnnouncementQueryDTO and AnnouncementService interface

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 5: 公告 ServiceImpl 单测（TDD：先写测试）

**Files:**
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/home/AnnouncementServiceImplTest.java`

- [ ] **Step 1: 创建测试目录并写测试**

```java
package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.AnnouncementQueryDTO;
import com.haifeng.app.service.impl.home.AnnouncementServiceImpl;
import com.haifeng.app.vo.home.AnnouncementDetailVO;
import com.haifeng.app.vo.home.AnnouncementListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Announcement;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.AnnouncementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceImplTest {

    @Mock private AnnouncementMapper announcementMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks private AnnouncementServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ===== 列表 =====

    @Test
    void list_cacheHit_returnsDirectlyWithoutQueryingDb() {
        AnnouncementListVO vo = new AnnouncementListVO();
        vo.setId(1L);
        vo.setTitle("cached");
        PageCacheDTO<AnnouncementListVO> cached =
                new PageCacheDTO<>(List.of(vo), 1L, 1L, 10L);

        String key = RedisKeyConstant.getAnnouncementListKey(1, 10, null);
        when(valueOps.get(key)).thenReturn(cached);

        AnnouncementQueryDTO dto = new AnnouncementQueryDTO();
        IPage<AnnouncementListVO> result = service.page(dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTitle()).isEqualTo("cached");
        verify(announcementMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_cacheMiss_queriesDbAndWritesCache() {
        String key = RedisKeyConstant.getAnnouncementListKey(1, 10, null);
        when(valueOps.get(key)).thenReturn(null);

        Announcement entity = Announcement.builder()
                .id(1L).title("t").tag("policy").status((short) 1)
                .updatedAt(OffsetDateTime.now()).deleted(false).build();
        Page<Announcement> mybatisPage = new Page<>(1, 10);
        mybatisPage.setRecords(List.of(entity));
        mybatisPage.setTotal(1);
        when(announcementMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenReturn(mybatisPage);

        IPage<AnnouncementListVO> result = service.page(new AnnouncementQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTitle()).isEqualTo("t");
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void list_withTagFilter_usesTagInCacheKey() {
        AnnouncementQueryDTO dto = new AnnouncementQueryDTO();
        dto.setTag("policy");
        String key = RedisKeyConstant.getAnnouncementListKey(1, 10, "policy");
        when(valueOps.get(key)).thenReturn(null);

        Page<Announcement> empty = new Page<>(1, 10);
        empty.setRecords(Collections.emptyList());
        when(announcementMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenReturn(empty);

        service.page(dto);

        verify(valueOps).get(key);
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    // ===== 详情 =====

    @Test
    void detail_cacheHit_returnsDirectly() {
        AnnouncementDetailVO cached = new AnnouncementDetailVO();
        cached.setId(1L);
        cached.setTitle("cached");
        when(valueOps.get(RedisKeyConstant.getAnnouncementDetailKey(1L))).thenReturn(cached);

        AnnouncementDetailVO result = service.detail(1L);

        assertThat(result.getTitle()).isEqualTo("cached");
        verify(announcementMapper, never()).selectById(anyLong());
    }

    @Test
    void detail_cacheMissAndExists_queriesDbAndCaches() {
        String key = RedisKeyConstant.getAnnouncementDetailKey(1L);
        when(valueOps.get(key)).thenReturn(null);
        Announcement entity = Announcement.builder()
                .id(1L).title("t").content("c").tag("g").status((short) 1).deleted(false).build();
        when(announcementMapper.selectById(1L)).thenReturn(entity);

        AnnouncementDetailVO result = service.detail(1L);

        assertThat(result.getTitle()).isEqualTo("t");
        verify(valueOps).set(eq(key), any(AnnouncementDetailVO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_notFound_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(announcementMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("公告不存在");
    }

    @Test
    void detail_statusZero_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        Announcement entity = Announcement.builder()
                .id(1L).title("t").status((short) 0).deleted(false).build();
        when(announcementMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("公告不存在");
    }
}
```

- [ ] **Step 2: 运行测试，确认全部失败（类还未实现）**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am test -Dtest=AnnouncementServiceImplTest -q`
Expected: 编译失败 — `AnnouncementServiceImpl` 不存在。

- [ ] **Step 3: 提交测试**

```bash
git add haifeng-app/src/test/java/com/haifeng/app/service/home/AnnouncementServiceImplTest.java
git commit -m "test(app/home): add failing tests for AnnouncementServiceImpl

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 6: 公告 ServiceImpl 实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/home/AnnouncementServiceImpl.java`

- [ ] **Step 1: 实现 ServiceImpl**

```java
package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.AnnouncementQueryDTO;
import com.haifeng.app.service.home.AnnouncementService;
import com.haifeng.app.vo.home.AnnouncementDetailVO;
import com.haifeng.app.vo.home.AnnouncementListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Announcement;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.AnnouncementMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final short STATUS_PUBLISHED = 1;

    private final AnnouncementMapper announcementMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public IPage<AnnouncementListVO> page(AnnouncementQueryDTO dto) {
        int pageNo = dto.getPage();
        int size = dto.getSize();
        String tag = dto.getTag();

        String cacheKey = RedisKeyConstant.getAnnouncementListKey(pageNo, size, tag);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageCacheDTO) {
            log.debug("公告列表缓存命中, key={}", cacheKey);
            PageCacheDTO<AnnouncementListVO> dtoCached = (PageCacheDTO<AnnouncementListVO>) cached;
            return toPage(dtoCached);
        }
        log.debug("公告列表缓存未命中, key={}", cacheKey);

        Page<Announcement> page = new Page<>(pageNo, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, STATUS_PUBLISHED);
        if (StringUtils.hasText(tag)) {
            wrapper.eq(Announcement::getTag, tag);
        }
        wrapper.orderByDesc(Announcement::getUpdatedAt);

        IPage<Announcement> entityPage = announcementMapper.selectPage(page, wrapper);
        IPage<AnnouncementListVO> voPage = entityPage.convert(this::toListVO);

        PageCacheDTO<AnnouncementListVO> toCache = new PageCacheDTO<>(
                voPage.getRecords(),
                voPage.getTotal(),
                voPage.getCurrent(),
                voPage.getSize()
        );
        redisTemplate.opsForValue().set(cacheKey, toCache,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return voPage;
    }

    @Override
    public AnnouncementDetailVO detail(Long id) {
        String cacheKey = RedisKeyConstant.getAnnouncementDetailKey(id);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof AnnouncementDetailVO) {
            log.debug("公告详情缓存命中, key={}", cacheKey);
            return (AnnouncementDetailVO) cached;
        }
        log.debug("公告详情缓存未命中, key={}", cacheKey);

        Announcement entity = announcementMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }

        AnnouncementDetailVO vo = new AnnouncementDetailVO();
        BeanUtils.copyProperties(entity, vo);

        redisTemplate.opsForValue().set(cacheKey, vo,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    private AnnouncementListVO toListVO(Announcement entity) {
        AnnouncementListVO vo = new AnnouncementListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private IPage<AnnouncementListVO> toPage(PageCacheDTO<AnnouncementListVO> cached) {
        Page<AnnouncementListVO> page = new Page<>(cached.getCurrent(), cached.getSize(), cached.getTotal());
        page.setRecords(cached.getRecords() == null
                ? java.util.Collections.emptyList()
                : cached.getRecords().stream().collect(Collectors.toList()));
        return page;
    }
}
```

- [ ] **Step 2: 运行测试，确认全绿**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am test -Dtest=AnnouncementServiceImplTest -q`
Expected: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/home/AnnouncementServiceImpl.java
git commit -m "feat(app/home): implement AnnouncementServiceImpl with Redis cache

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 7: 公告 Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/home/AnnouncementController.java`

- [ ] **Step 1: 实现 Controller**

```java
package com.haifeng.app.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.AnnouncementQueryDTO;
import com.haifeng.app.service.home.AnnouncementService;
import com.haifeng.app.vo.home.AnnouncementDetailVO;
import com.haifeng.app.vo.home.AnnouncementListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端首页 - 公告（公开接口，无需登录）
 */
@RestController
@RequestMapping("/api/v1/app/home/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /** 分页查询公告列表 */
    @GetMapping
    public R<IPage<AnnouncementListVO>> list(@Valid AnnouncementQueryDTO dto) {
        return R.ok(announcementService.page(dto));
    }

    /** 公告详情 */
    @GetMapping("/{id}")
    public R<AnnouncementDetailVO> detail(@PathVariable Long id) {
        return R.ok(announcementService.detail(id));
    }
}
```

- [ ] **Step 2: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/home/AnnouncementController.java
git commit -m "feat(app/home): add AnnouncementController (public list + detail)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 8: 规划师 VO（List + Detail）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/home/PlannerListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/home/PlannerDetailVO.java`

- [ ] **Step 1: 新建 `PlannerListVO`**

```java
package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlannerListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String region;
    private String position;
    private String avatar;
    private String specialty;
    private String personalDescription;
}
```

- [ ] **Step 2: 新建 `PlannerDetailVO`**

```java
package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PlannerDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String position;
    private String region;
    private String avatar;
    private String specialty;
    private String douyinName;
    private String douyinUrl;
    private String personalDescription;
    private String experienceJob;
    private List<String> achievements;
    private List<String> expertiseAreas;
}
```

- [ ] **Step 3: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/home/PlannerListVO.java haifeng-app/src/main/java/com/haifeng/app/vo/home/PlannerDetailVO.java
git commit -m "feat(app/home): add PlannerListVO and PlannerDetailVO

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 9: 规划师 QueryDTO + Service 接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/home/PlannerQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/home/PlannerService.java`

- [ ] **Step 1: 新建 `PlannerQueryDTO`**

```java
package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlannerQueryDTO extends BasePageQueryDTO {

    /** 所在地区，必须是 ProvinceEnum 中文 desc（可选） */
    private String region;
}
```

- [ ] **Step 2: 新建 `PlannerService` 接口**

```java
package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;

public interface PlannerService {

    /** 分页查询展示中的规划师（status=1），按 sort_order ASC, id DESC 排序 */
    IPage<PlannerListVO> page(PlannerQueryDTO dto);

    /** 查询规划师详情（仅 status=1，不存在抛 404） */
    PlannerDetailVO detail(Long id);
}
```

- [ ] **Step 3: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/home/PlannerQueryDTO.java haifeng-app/src/main/java/com/haifeng/app/service/home/PlannerService.java
git commit -m "feat(app/home): add PlannerQueryDTO and PlannerService interface

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 10: 规划师 ServiceImpl 单测

**Files:**
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/home/PlannerServiceImplTest.java`

- [ ] **Step 1: 创建测试文件**

```java
package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.service.impl.home.PlannerServiceImpl;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannerServiceImplTest {

    @Mock private PlannerMapper plannerMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks private PlannerServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ===== region 校验 =====

    @Test
    void list_invalidRegion_throws400_doesNotTouchCacheOrDb() {
        PlannerQueryDTO dto = new PlannerQueryDTO();
        dto.setRegion("火星");

        assertThatThrownBy(() -> service.page(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无效的省份");

        verify(redisTemplate, never()).opsForValue();
        verify(plannerMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_nullRegion_noValidationError_proceeds() {
        when(valueOps.get(anyString())).thenReturn(null);
        Page<Planner> empty = new Page<>(1, 10);
        empty.setRecords(Collections.emptyList());
        when(plannerMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(empty);

        service.page(new PlannerQueryDTO());  // region=null

        verify(plannerMapper).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    void list_validRegion_proceedsToQuery() {
        PlannerQueryDTO dto = new PlannerQueryDTO();
        dto.setRegion("北京");

        when(valueOps.get(anyString())).thenReturn(null);
        Page<Planner> empty = new Page<>(1, 10);
        empty.setRecords(Collections.emptyList());
        when(plannerMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(empty);

        service.page(dto);

        String key = RedisKeyConstant.getPlannerListKey(1, 10, "北京");
        verify(valueOps).get(key);
    }

    // ===== 缓存 =====

    @Test
    void list_cacheHit_returnsDirectly() {
        PlannerListVO vo = new PlannerListVO();
        vo.setId(1L);
        vo.setName("cached");
        PageCacheDTO<PlannerListVO> cached =
                new PageCacheDTO<>(List.of(vo), 1L, 1L, 10L);
        when(valueOps.get(anyString())).thenReturn(cached);

        IPage<PlannerListVO> result = service.page(new PlannerQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("cached");
        verify(plannerMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_cacheMiss_queriesDbAndWritesCache() {
        String key = RedisKeyConstant.getPlannerListKey(1, 10, null);
        when(valueOps.get(key)).thenReturn(null);

        Planner entity = Planner.builder()
                .id(1L).name("张老师").region("北京").status((short) 1).deleted(false).build();
        Page<Planner> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(plannerMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        IPage<PlannerListVO> result = service.page(new PlannerQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    // ===== 详情 =====

    @Test
    void detail_cacheHit_returnsDirectly() {
        PlannerDetailVO cached = new PlannerDetailVO();
        cached.setId(1L);
        cached.setName("cached");
        when(valueOps.get(RedisKeyConstant.getPlannerDetailKey(1L))).thenReturn(cached);

        PlannerDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("cached");
        verify(plannerMapper, never()).selectById(anyLong());
    }

    @Test
    void detail_cacheMissAndExists_queriesDbAndCaches() {
        String key = RedisKeyConstant.getPlannerDetailKey(1L);
        when(valueOps.get(key)).thenReturn(null);
        Planner entity = Planner.builder()
                .id(1L).name("张老师").status((short) 1).deleted(false).build();
        when(plannerMapper.selectById(1L)).thenReturn(entity);

        PlannerDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("张老师");
        verify(valueOps).set(eq(key), any(PlannerDetailVO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_notFound_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(plannerMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("规划师不存在");
    }

    @Test
    void detail_statusZero_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        Planner entity = Planner.builder()
                .id(1L).name("张老师").status((short) 0).deleted(false).build();
        when(plannerMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("规划师不存在");
    }
}
```

- [ ] **Step 2: 运行测试，确认编译失败（ServiceImpl 未实现）**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am test -Dtest=PlannerServiceImplTest -q`
Expected: 编译失败 — `PlannerServiceImpl` 不存在

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/test/java/com/haifeng/app/service/home/PlannerServiceImplTest.java
git commit -m "test(app/home): add failing tests for PlannerServiceImpl

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 11: 规划师 ServiceImpl 实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/home/PlannerServiceImpl.java`

- [ ] **Step 1: 实现 ServiceImpl**

```java
package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.service.home.PlannerService;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlannerServiceImpl implements PlannerService {

    private static final short STATUS_PUBLISHED = 1;

    private final PlannerMapper plannerMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public IPage<PlannerListVO> page(PlannerQueryDTO dto) {
        // 1) 参数校验：region 必须在 ProvinceEnum 中（null 视为合法）
        if (dto.getRegion() != null && !ProvinceEnum.isValid(dto.getRegion())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的省份");
        }

        int pageNo = dto.getPage();
        int size = dto.getSize();
        String region = dto.getRegion();

        // 2) 缓存查询
        String cacheKey = RedisKeyConstant.getPlannerListKey(pageNo, size, region);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageCacheDTO) {
            log.debug("规划师列表缓存命中, key={}", cacheKey);
            return toPage((PageCacheDTO<PlannerListVO>) cached);
        }
        log.debug("规划师列表缓存未命中, key={}", cacheKey);

        // 3) DB 查询
        Page<Planner> page = new Page<>(pageNo, size);
        LambdaQueryWrapper<Planner> wrapper = new LambdaQueryWrapper<Planner>()
                .eq(Planner::getStatus, STATUS_PUBLISHED);
        if (region != null) {
            wrapper.eq(Planner::getRegion, region);
        }
        wrapper.orderByAsc(Planner::getSortOrder)
               .orderByDesc(Planner::getId);

        IPage<Planner> entityPage = plannerMapper.selectPage(page, wrapper);
        IPage<PlannerListVO> voPage = entityPage.convert(this::toListVO);

        // 4) 写缓存
        PageCacheDTO<PlannerListVO> toCache = new PageCacheDTO<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize());
        redisTemplate.opsForValue().set(cacheKey, toCache,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return voPage;
    }

    @Override
    public PlannerDetailVO detail(Long id) {
        String cacheKey = RedisKeyConstant.getPlannerDetailKey(id);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PlannerDetailVO) {
            log.debug("规划师详情缓存命中, key={}", cacheKey);
            return (PlannerDetailVO) cached;
        }
        log.debug("规划师详情缓存未命中, key={}", cacheKey);

        Planner entity = plannerMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "规划师不存在");
        }

        PlannerDetailVO vo = new PlannerDetailVO();
        BeanUtils.copyProperties(entity, vo);

        redisTemplate.opsForValue().set(cacheKey, vo,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    private PlannerListVO toListVO(Planner entity) {
        PlannerListVO vo = new PlannerListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private IPage<PlannerListVO> toPage(PageCacheDTO<PlannerListVO> cached) {
        Page<PlannerListVO> page = new Page<>(cached.getCurrent(), cached.getSize(), cached.getTotal());
        page.setRecords(cached.getRecords() == null
                ? Collections.emptyList()
                : cached.getRecords().stream().collect(Collectors.toList()));
        return page;
    }
}
```

- [ ] **Step 2: 运行测试，确认全绿**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am test -Dtest=PlannerServiceImplTest -q`
Expected: Tests run: 9, Failures: 0, Errors: 0, Skipped: 0

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/home/PlannerServiceImpl.java
git commit -m "feat(app/home): implement PlannerServiceImpl with region validation and Redis cache

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 12: 规划师 Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/home/PlannerController.java`

- [ ] **Step 1: 实现 Controller**

```java
package com.haifeng.app.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.service.home.PlannerService;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端首页 - 规划师（公开接口，无需登录）
 */
@RestController
@RequestMapping("/api/v1/app/home/planners")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    /** 分页查询规划师列表 */
    @GetMapping
    public R<IPage<PlannerListVO>> list(@Valid PlannerQueryDTO dto) {
        return R.ok(plannerService.page(dto));
    }

    /** 规划师详情 */
    @GetMapping("/{id}")
    public R<PlannerDetailVO> detail(@PathVariable Long id) {
        return R.ok(plannerService.detail(id));
    }
}
```

- [ ] **Step 2: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/home/PlannerController.java
git commit -m "feat(app/home): add PlannerController (public list + detail)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 13: 培训机构 VO（List + Detail）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/home/InstitutionListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/home/InstitutionDetailVO.java`

- [ ] **Step 1: 新建 `InstitutionListVO`**

```java
package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InstitutionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String type;
    private String description;
    private List<String> images;
}
```

- [ ] **Step 2: 新建 `InstitutionDetailVO`**

```java
package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InstitutionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String type;
    private String phone;
    private String address;
    private String description;
    private List<String> courses;
    private List<String> images;
    private String logo;
}
```

- [ ] **Step 3: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/home/InstitutionListVO.java haifeng-app/src/main/java/com/haifeng/app/vo/home/InstitutionDetailVO.java
git commit -m "feat(app/home): add InstitutionListVO and InstitutionDetailVO

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 14: 培训机构 QueryDTO + Service 接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/home/InstitutionQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/home/InstitutionService.java`

- [ ] **Step 1: 新建 `InstitutionQueryDTO`**

```java
package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 培训机构列表查询 DTO（C 端目前无业务筛选字段，保留 class 便于后续扩展）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionQueryDTO extends BasePageQueryDTO {
}
```

- [ ] **Step 2: 新建 `InstitutionService` 接口**

```java
package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;

public interface InstitutionService {

    /** 分页查询展示中的培训机构（status=1），按 sort_order ASC, id DESC 排序 */
    IPage<InstitutionListVO> page(InstitutionQueryDTO dto);

    /** 查询培训机构详情（仅 status=1，不存在抛 404） */
    InstitutionDetailVO detail(Long id);
}
```

- [ ] **Step 3: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/home/InstitutionQueryDTO.java haifeng-app/src/main/java/com/haifeng/app/service/home/InstitutionService.java
git commit -m "feat(app/home): add InstitutionQueryDTO and InstitutionService interface

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 15: 培训机构 ServiceImpl 单测

**Files:**
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/home/InstitutionServiceImplTest.java`

- [ ] **Step 1: 创建测试文件**

```java
package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.service.impl.home.InstitutionServiceImpl;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @Mock private InstitutionMapper institutionMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks private InstitutionServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void list_cacheHit_returnsDirectly() {
        InstitutionListVO vo = new InstitutionListVO();
        vo.setId(1L);
        vo.setName("cached");
        PageCacheDTO<InstitutionListVO> cached =
                new PageCacheDTO<>(List.of(vo), 1L, 1L, 10L);
        when(valueOps.get(anyString())).thenReturn(cached);

        IPage<InstitutionListVO> result = service.page(new InstitutionQueryDTO());

        assertThat(result.getRecords().get(0).getName()).isEqualTo("cached");
        verify(institutionMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_cacheMiss_queriesDbAndWritesCache() {
        String key = RedisKeyConstant.getInstitutionListKey(1, 10);
        when(valueOps.get(key)).thenReturn(null);

        Institution entity = Institution.builder()
                .id(1L).name("机构A").type("职业培训").status((short) 1).deleted(false).build();
        Page<Institution> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(institutionMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        IPage<InstitutionListVO> result = service.page(new InstitutionQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_cacheHit_returnsDirectly() {
        InstitutionDetailVO cached = new InstitutionDetailVO();
        cached.setId(1L);
        cached.setName("cached");
        when(valueOps.get(RedisKeyConstant.getInstitutionDetailKey(1L))).thenReturn(cached);

        InstitutionDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("cached");
        verify(institutionMapper, never()).selectById(anyLong());
    }

    @Test
    void detail_cacheMissAndExists_queriesDbAndCaches() {
        String key = RedisKeyConstant.getInstitutionDetailKey(1L);
        when(valueOps.get(key)).thenReturn(null);
        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 1).deleted(false).build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        InstitutionDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("机构A");
        verify(valueOps).set(eq(key), any(InstitutionDetailVO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_notFound_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(institutionMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }

    @Test
    void detail_statusZero_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 0).deleted(false).build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }
}
```

- [ ] **Step 2: 运行测试，确认编译失败**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am test -Dtest=InstitutionServiceImplTest -q`
Expected: 编译失败 — `InstitutionServiceImpl` 不存在

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/test/java/com/haifeng/app/service/home/InstitutionServiceImplTest.java
git commit -m "test(app/home): add failing tests for InstitutionServiceImpl

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 16: 培训机构 ServiceImpl 实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/home/InstitutionServiceImpl.java`

- [ ] **Step 1: 实现 ServiceImpl**

```java
package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.service.home.InstitutionService;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private static final short STATUS_PUBLISHED = 1;

    private final InstitutionMapper institutionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public IPage<InstitutionListVO> page(InstitutionQueryDTO dto) {
        int pageNo = dto.getPage();
        int size = dto.getSize();

        String cacheKey = RedisKeyConstant.getInstitutionListKey(pageNo, size);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageCacheDTO) {
            log.debug("培训机构列表缓存命中, key={}", cacheKey);
            return toPage((PageCacheDTO<InstitutionListVO>) cached);
        }
        log.debug("培训机构列表缓存未命中, key={}", cacheKey);

        Page<Institution> page = new Page<>(pageNo, size);
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<Institution>()
                .eq(Institution::getStatus, STATUS_PUBLISHED)
                .orderByAsc(Institution::getSortOrder)
                .orderByDesc(Institution::getId);

        IPage<Institution> entityPage = institutionMapper.selectPage(page, wrapper);
        IPage<InstitutionListVO> voPage = entityPage.convert(this::toListVO);

        PageCacheDTO<InstitutionListVO> toCache = new PageCacheDTO<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize());
        redisTemplate.opsForValue().set(cacheKey, toCache,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return voPage;
    }

    @Override
    public InstitutionDetailVO detail(Long id) {
        String cacheKey = RedisKeyConstant.getInstitutionDetailKey(id);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof InstitutionDetailVO) {
            log.debug("培训机构详情缓存命中, key={}", cacheKey);
            return (InstitutionDetailVO) cached;
        }
        log.debug("培训机构详情缓存未命中, key={}", cacheKey);

        Institution entity = institutionMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "培训机构不存在");
        }

        InstitutionDetailVO vo = new InstitutionDetailVO();
        BeanUtils.copyProperties(entity, vo);

        redisTemplate.opsForValue().set(cacheKey, vo,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    private InstitutionListVO toListVO(Institution entity) {
        InstitutionListVO vo = new InstitutionListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private IPage<InstitutionListVO> toPage(PageCacheDTO<InstitutionListVO> cached) {
        Page<InstitutionListVO> page = new Page<>(cached.getCurrent(), cached.getSize(), cached.getTotal());
        page.setRecords(cached.getRecords() == null
                ? Collections.emptyList()
                : cached.getRecords().stream().collect(Collectors.toList()));
        return page;
    }
}
```

- [ ] **Step 2: 运行测试，确认全绿**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am test -Dtest=InstitutionServiceImplTest -q`
Expected: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/home/InstitutionServiceImpl.java
git commit -m "feat(app/home): implement InstitutionServiceImpl with Redis cache

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 17: 培训机构 Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/home/InstitutionController.java`

- [ ] **Step 1: 实现 Controller**

```java
package com.haifeng.app.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.service.home.InstitutionService;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端首页 - 培训机构（公开接口，无需登录）
 */
@RestController
@RequestMapping("/api/v1/app/home/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    /** 分页查询培训机构列表 */
    @GetMapping
    public R<IPage<InstitutionListVO>> list(@Valid InstitutionQueryDTO dto) {
        return R.ok(institutionService.page(dto));
    }

    /** 培训机构详情 */
    @GetMapping("/{id}")
    public R<InstitutionDetailVO> detail(@PathVariable Long id) {
        return R.ok(institutionService.detail(id));
    }
}
```

- [ ] **Step 2: 编译通过**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 全量测试**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am test -Dtest="*ServiceImplTest" -q`
Expected: 全部 home 模块测试通过（约 21 个用例）

- [ ] **Step 4: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/home/InstitutionController.java
git commit -m "feat(app/home): add InstitutionController (public list + detail)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 18: 手动验证

> 此 Task 不涉及代码改动，目的是把 6 个接口跑一遍真实数据。需要先确保本地 PostgreSQL + Redis 启动、数据库中 `t_announcements` / `t_planners` / `t_institutions` 各至少有一条 `status=1` 的记录。

- [ ] **Step 1: 启动 app 服务**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am spring-boot:run -q`
Expected: 看到 `Started HaiFengAppApplication`

- [ ] **Step 2: 验证 6 个场景**

新开一个终端，依次执行（端口按 `application.yml` 实际为准，默认 `8080`）：

| # | 命令 | 预期 |
|---|------|------|
| 1 | `curl -s http://localhost:8080/api/v1/app/home/announcements \| jq` | `code:200`，`data.records` 是数组 |
| 2 | 重复 1 后执行 `redis-cli KEYS "haifeng:app:home:*"` | 看到 `haifeng:app:home:announcement:list:p=1:s=10:tag=` |
| 3 | `curl -s "http://localhost:8080/api/v1/app/home/planners?region=北京" \| jq` | `code:200` |
| 4 | `curl -s "http://localhost:8080/api/v1/app/home/planners?region=火星" \| jq` | `code:400`，`msg:"无效的省份"` |
| 5 | `curl -s http://localhost:8080/api/v1/app/home/announcements/999999 \| jq` | `code:404`，`msg:"公告不存在"` |
| 6 | `curl -s http://localhost:8080/api/v1/app/home/institutions \| jq` | `code:200` |

- [ ] **Step 3: 验证 TTL（可选）**

  - admin 端改一条公告 title
  - 立即 `curl /announcements` → 仍是旧 title（缓存未过期）
  - `redis-cli DEL` 清掉对应 key（或等 30 分钟）→ 再 `curl` → 新 title

- [ ] **Step 4: 关闭服务**

Ctrl+C 关闭 `mvn spring-boot:run`

无需 commit（纯验证）。

---

## 完成标准

- 19 个新文件 + 1 个修改文件全部在 git 历史中
- 三个 ServiceImpl 单测全部通过（共 21 个用例）
- Task 18 的 6 个手动验证场景全部观察到预期结果
- 未引入新的运行时异常（启动日志干净）

---

## 自检

**Spec 覆盖**：
- ✓ 任务 1-1/2-1/3-1（列表）→ Task 6/11/16 实现 + Task 7/12/17 暴露
- ✓ 任务 1-2/2-2/3-2（详情）→ 同上
- ✓ 任务 4（缓存）→ Task 1-2（基础设施）+ 三个 ServiceImpl 内的读写逻辑
- ✓ tag 精准筛选 → Task 5 测试 + Task 6 `wrapper.eq(tag)`
- ✓ region 必须是 `ProvinceEnum` → Task 10 测试 + Task 11 校验
- ✓ status=1 强制过滤 → 三个 ServiceImpl 都有 `STATUS_PUBLISHED`
- ✓ 详情 404 → 三个 ServiceImpl 都有判断 + 单测覆盖
- ✓ 无需登录 → Controller 不加任何权限注解
- ✓ 列表按 sort_order + id 排序（规划师 / 机构）→ Task 11 / 16 `orderByAsc + orderByDesc`

**类型/方法一致性**：
- ✓ `RedisKeyConstant.getXxxKey(...)` 签名在 Task 2 定义、Task 5/6/10/11/15/16 使用一致
- ✓ `PageCacheDTO` 字段 `(records, total, current, size)` 跨所有 Task 一致
- ✓ `ServiceImpl` 方法名 `page(dto)` / `detail(id)` 与接口、测试、Controller 一致

**无占位符**：所有 step 含完整代码或命令；无 TBD/TODO。
