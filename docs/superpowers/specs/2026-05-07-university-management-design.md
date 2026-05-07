# 院校管理模块设计文档

**日期：** 2026-05-07
**模块：** 院校管理（university）
**状态：** 待实现

---

## 1. 概述

### 1.1 功能范围

本次实现后台管理端的院校管理模块，包含以下子模块：
- 院校列表（universities + universities_detail）
- 校园图册（t_campus_gallery）
- 院校适应指南（university_guides）

### 1.2 核心功能

| 功能 | 说明 |
|------|------|
| 分页列表 | 带筛选条件的分页查询 |
| 详情查看 | Tab式展示基础信息和详细介绍 |
| 增删改 | 支持单条和批量删除（软删除） |
| Excel导入 | 支持xlsx批量导入，使用EasyExcel |

---

## 2. 数据库设计

### 2.1 表结构

#### universities（院校主表）

```sql
CREATE TABLE universities (
    id                  BIGINT        PRIMARY KEY,
    name                VARCHAR(50)   NOT NULL,
    name_en             VARCHAR(50)   NOT NULL,
    province_name       VARCHAR(50)   NOT NULL,
    city_name           VARCHAR(50)   NOT NULL,
    region              VARCHAR(50)   NOT NULL,
    category            VARCHAR(50)   NOT NULL,
    major_count         INTEGER       DEFAULT 0,
    education_level     VARCHAR(50),
    nature              VARCHAR(50),
    recommendation_rate DECIMAL(5,2),
    recommendation_year INTEGER,
    has_doctorate       BOOLEAN       DEFAULT false,
    has_master          BOOLEAN       DEFAULT false,
    department          VARCHAR(100),
    tags                TEXT[],
    famous_union        VARCHAR(50),
    image_url           VARCHAR(500),
    introduction        TEXT,
    sort_order          INTEGER       DEFAULT 0,
    status              SMALLINT      DEFAULT 1 NOT NULL,
    created_at          TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at          TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

#### universities_detail（院校详情表）

```sql
CREATE TABLE universities_detail (
    id                    BIGINT        PRIMARY KEY,
    university_id         BIGINT        NOT NULL UNIQUE,
    address               VARCHAR(200),
    admission_phone       VARCHAR(50),
    website               VARCHAR(500),
    history_group_score   INTEGER,
    science_group_score   INTEGER,
    carousel_images       TEXT[],
    introduction          TEXT,
    rankings              JSONB         DEFAULT '{}'::JSONB,
    abroad_rate           VARCHAR(10),
    gender_ratio          VARCHAR(10),
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

**rankings JSONB 结构：**
```json
{
  "ruanke": 15,
  "xiaoyouhui": 18,
  "wushulian": 20,
  "qs": 250,
  "usnews": 280
}
```

#### t_campus_gallery（校园图册表）

```sql
CREATE TABLE t_campus_gallery (
    id                  BIGINT          PRIMARY KEY,
    university_id       BIGINT          NOT NULL,
    university_name     VARCHAR(30)     NOT NULL,
    image_type          VARCHAR(30)     NOT NULL,
    image_url           VARCHAR(500)    NOT NULL,
    sort_order          INTEGER         DEFAULT 0,
    status              SMALLINT        DEFAULT 1 NOT NULL,
    created_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

#### university_guides（院校适应指南表）

```sql
CREATE TABLE university_guides (
    id                          BIGINT        PRIMARY KEY,
    university_id               BIGINT        NOT NULL UNIQUE,
    custom_tags                 TEXT[],
    campus_facilities           JSONB         DEFAULT '{}'::JSONB,
    dormitory_services          JSONB         DEFAULT '{}'::JSONB,
    campus_transportation       JSONB         DEFAULT '{}'::JSONB,
    academic_guidance           JSONB         DEFAULT '{}'::JSONB,
    major_transfer_guidelines   JSONB         DEFAULT '{}'::JSONB,
    major_transfer_constriction JSONB         DEFAULT '{}'::JSONB,
    academic_support_resources  JSONB         DEFAULT '{}'::JSONB,
    student_organizations       JSONB         DEFAULT '{}'::JSONB,
    campus_events               JSONB         DEFAULT '{}'::JSONB,
    class_dorm_social           JSONB         DEFAULT '{}'::JSONB,
    financial_aid               JSONB         DEFAULT '{}'::JSONB,
    campus_security             JSONB         DEFAULT '{}'::JSONB,
    health_services             JSONB         DEFAULT '{}'::JSONB,
    life_services               JSONB         DEFAULT '{}'::JSONB,
    remark                      TEXT,
    status                      SMALLINT      DEFAULT 1 NOT NULL,
    created_at                  TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at                  TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

### 2.2 表关系

| 主表 | 从表 | 关系 |
|------|------|------|
| universities | universities_detail | 1:1 |
| universities | t_campus_gallery | 1:N |
| universities | university_guides | 1:1 |

---

## 3. 代码结构

### 3.1 haifeng-admin 包结构

```
com.haifeng.admin/
├── controller/university/
│   ├── UniversityController.java
│   ├── CampusGalleryController.java
│   └── UniversityGuideController.java
├── service/university/
│   ├── UniversityService.java
│   ├── CampusGalleryService.java
│   └── UniversityGuideService.java
├── service/impl/university/
│   ├── UniversityServiceImpl.java
│   ├── CampusGalleryServiceImpl.java
│   └── UniversityGuideServiceImpl.java
├── dto/university/
│   ├── UniversityQueryDTO.java
│   ├── UniversityAddDTO.java
│   ├── UniversityUpdateDTO.java
│   ├── UniversityDetailUpdateDTO.java
│   ├── CampusGalleryQueryDTO.java
│   ├── CampusGalleryAddDTO.java
│   ├── CampusGalleryUpdateDTO.java
│   ├── UniversityGuideQueryDTO.java
│   ├── UniversityGuideAddDTO.java
│   └── UniversityGuideUpdateDTO.java
├── vo/university/
│   ├── UniversityListVO.java
│   ├── UniversityDetailVO.java
│   ├── CampusGalleryListVO.java
│   ├── UniversityGuideListVO.java
│   └── UniversityGuideDetailVO.java
└── excel/university/
    ├── UniversityExcelDTO.java
    ├── UniversityDetailExcelDTO.java
    ├── CampusGalleryExcelDTO.java
    ├── UniversityGuideExcelDTO.java
    ├── GuideJsonbExcelDTO.java
    └── StringArrayConverter.java
```

### 3.2 haifeng-common 包结构

```
com.haifeng.common/
├── entity/university/
│   ├── University.java
│   ├── UniversityDetail.java
│   ├── CampusGallery.java
│   └── UniversityGuide.java
└── mapper/university/
    ├── UniversityMapper.java
    ├── UniversityDetailMapper.java
    ├── CampusGalleryMapper.java
    └── UniversityGuideMapper.java
```

---

## 4. API 设计

### 4.1 院校管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/university/list` | 分页查询院校列表 |
| GET | `/api/v1/admin/university/{id}` | 获取院校详情 |
| POST | `/api/v1/admin/university` | 新增院校 |
| PUT | `/api/v1/admin/university/{id}` | 修改院校基础信息 |
| PUT | `/api/v1/admin/university/{id}/detail` | 修改院校详情信息 |
| DELETE | `/api/v1/admin/university/{id}` | 删除院校 |
| DELETE | `/api/v1/admin/university/batch` | 批量删除院校 |
| POST | `/api/v1/admin/university/import` | 导入院校主表xlsx |
| POST | `/api/v1/admin/university/import-detail` | 导入院校详情xlsx |

### 4.2 校园图册接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/university/gallery/list` | 分页查询图册列表 |
| POST | `/api/v1/admin/university/gallery` | 新增图片 |
| PUT | `/api/v1/admin/university/gallery/{id}` | 修改图片 |
| DELETE | `/api/v1/admin/university/gallery/{id}` | 删除图片 |
| DELETE | `/api/v1/admin/university/gallery/batch` | 批量删除图片 |
| POST | `/api/v1/admin/university/gallery/import` | 导入图册xlsx |

### 4.3 院校适应指南接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/university/guide/list` | 分页查询指南列表 |
| GET | `/api/v1/admin/university/guide/{id}` | 获取指南详情 |
| POST | `/api/v1/admin/university/guide` | 新增指南 |
| PUT | `/api/v1/admin/university/guide/{id}` | 修改指南 |
| DELETE | `/api/v1/admin/university/guide/{id}` | 删除指南 |
| DELETE | `/api/v1/admin/university/guide/batch` | 批量删除指南 |
| POST | `/api/v1/admin/university/guide/import` | 导入指南xlsx |

### 4.4 查询参数

**UniversityQueryDTO：**
- `name`: 院校名称（模糊搜索）
- `provinceName`: 省份
- `category`: 院校类别
- `status`: 状态
- `page`: 页码
- `size`: 每页条数

**CampusGalleryQueryDTO：**
- `universityName`: 院校名称（模糊搜索）
- `imageType`: 图片类型
- `page`, `size`

**UniversityGuideQueryDTO：**
- `universityName`: 院校名称（模糊搜索）
- `status`: 状态
- `page`, `size`

---

## 5. Excel 导入设计

### 5.1 依赖

```xml
<!-- haifeng-common/pom.xml -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>4.0.3</version>
</dependency>
```

### 5.2 配置

```yaml
# application-dev.yml
spring:
  servlet:
    multipart:
      max-file-size: 30MB
      max-request-size: 40MB
```

### 5.3 xlsx表头规范

#### xlsx1：院校主表

| 表头名称 | 字段 | 类型 | 必填 |
|---------|------|------|------|
| 院校名称 | name | VARCHAR(50) | Y |
| 院校名称英文 | name_en | VARCHAR(50) | Y |
| 省份 | province_name | VARCHAR(50) | Y |
| 城市 | city_name | VARCHAR(50) | Y |
| 所属地区 | region | VARCHAR(50) | Y |
| 院校类别 | category | VARCHAR(50) | Y |
| 专业数量 | major_count | INTEGER | |
| 办学层次 | education_level | VARCHAR(50) | |
| 院校性质 | nature | VARCHAR(50) | |
| 是否有博士点 | has_doctorate | BOOLEAN | |
| 是否有硕士点 | has_master | BOOLEAN | |
| 隶属部门 | department | VARCHAR(100) | |
| 院校标签 | tags | TEXT[] | |
| 知名联盟 | famous_union | VARCHAR(50) | |
| 院校图片URL | image_url | VARCHAR(500) | |
| 院校简介 | introduction | TEXT | |
| 推免率 | recommendation_rate | DECIMAL(5,2) | |
| 推免年份 | recommendation_year | INTEGER | |

#### xlsx2：院校详情表

| 表头名称 | 字段 | 类型 | 必填 |
|---------|------|------|------|
| 院校名称 | →university_id | VARCHAR | Y |
| 学校地址 | address | VARCHAR(200) | |
| 招生电话 | admission_phone | VARCHAR(50) | |
| 官方网站 | website | VARCHAR(500) | |
| 本科批历史组 | history_group_score | INTEGER | |
| 本科批物理组 | science_group_score | INTEGER | |
| 轮播图片URL | carousel_images | TEXT[] | |
| 院校详细介绍 | introduction | TEXT | |
| 软科排名 | rankings.ruanke | INTEGER | |
| 校友会排名 | rankings.xiaoyouhui | INTEGER | |
| 武书连排名 | rankings.wushulian | INTEGER | |
| QS排名 | rankings.qs | INTEGER | |
| U.S.NEWS排名 | rankings.usnews | INTEGER | |
| 出国比例 | abroad_rate | VARCHAR(10) | |
| 男女比例 | gender_ratio | VARCHAR(10) | |

#### xlsx3：校园图册表

| 表头名称 | 字段 | 类型 | 必填 |
|---------|------|------|------|
| 院校名称 | →university_id | VARCHAR | Y |
| 图片类型 | image_type | VARCHAR(30) | Y |
| 图片URL | image_url | VARCHAR(500) | Y |
| 排序权重 | sort_order | INTEGER | |

#### xlsx4：院校适应指南表

**Sheet0（主表）：**
| 表头名称 | 字段 | 必填 |
|---------|------|------|
| 院校名称 | →university_id | Y |
| 自定义标签 | custom_tags | |
| 备注 | remark | |
| 状态 | status | |

**Sheet1-14（JSONB字段）：**

每个Sheet对应一个JSONB字段，通过"院校名称"关联主表。

| Sheet | JSONB字段 | 子字段 |
|-------|----------|--------|
| Sheet1 | class_dorm_social | 班级管理方式, 宿舍社交建议 |
| Sheet2 | financial_aid | 奖助学金政策, 勤工俭学岗位, 权益申诉渠道 |
| Sheet3 | life_services | 校园生活服务, 医疗资源, 兼职实习资源 |
| Sheet4 | dormitory_services | 水电费缴纳方式, 宿舍规章制度 |
| Sheet5 | campus_security | 安全设施, 安全规则 |
| Sheet6 | campus_events | 院校品牌活动, 学科与技能竞赛 |
| Sheet7 | campus_facilities | 教学楼分布, 实验楼与图书馆, 宿舍区与食堂, 生活配套设施 |
| Sheet8 | campus_transportation | 校内通勤方式, 校外交通情况 |
| Sheet9 | student_organizations | 官方组织, 社团类型 |
| Sheet10 | academic_support_resources | 师资力量, 学习场所, 学业帮扶 |
| Sheet11 | health_services | 医保报销政策, 心理健康服务 |
| Sheet12 | academic_guidance | 专业培养方案说明, 选课系统说明 |
| Sheet13 | major_transfer_constriction | 限制类型, 具体限制说明 |
| Sheet14 | major_transfer_guidelines | 基本申请条件, 申请时间与流程 |

### 5.4 StringArrayConverter

```java
public class StringArrayConverter implements Converter<String[]> {
    @Override
    public String[] convertToJavaData(ReadCellData<?> cellData,
            ExcelContentProperty contentProperty,
            GlobalConfiguration globalConfiguration) {
        String val = cellData.getStringValue();
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        // 支持中英文逗号分隔
        return val.split("[,，]");
    }
}
```

### 5.5 导入流程

```
1. 接收MultipartFile
2. 使用EasyExcel读取数据到List<ExcelDTO>
3. 校验表头规范性
4. 校验数据：
   - 必填字段不能为空
   - 主表：unique字段不能重复（数据库+Excel内）
   - 外键表：院校名称必须在主表中存在
   - 1:1外键表：院校名称不能重复
5. 收集所有错误到errorMsgs
6. 如有错误 → 抛出BusinessException
7. 无错误 → 转换为Entity → saveBatch
8. @Transactional保证一致性
```

---

## 6. 错误处理

### 6.1 错误信息格式

```json
{
  "code": 400,
  "msg": "导入失败：第3行：'院校名称'不能为空；第5行：院校名称'未知大学'在主表中不存在",
  "data": null,
  "timestamp": 1699123456789
}
```

### 6.2 校验规则

| 校验类型 | 错误示例 |
|---------|---------|
| 必填校验 | 第X行：'院校名称'不能为空 |
| 唯一性校验 | 第X行：院校名称'XXX'已存在 |
| Excel内重复 | 第X行：院校名称'XXX'与第Y行重复 |
| 外键校验 | 第X行：院校名称'XXX'在主表中不存在 |
| 类型校验 | 第X行：'专业数量'应为整数 |
| 范围校验 | 第X行：'院校类别'值'XXX'不在允许范围内 |

---

## 7. VO 字段设计

### UniversityListVO

```java
Long id;
String name;
String provinceName;
String cityName;
String region;
String category;
Integer majorCount;
String educationLevel;
String nature;
Integer status;
LocalDateTime createdAt;
```

### UniversityDetailVO

```java
// 基础信息（Tab1）
Long id;
String name;
String nameEn;
String provinceName;
String cityName;
String region;
String category;
Integer majorCount;
String educationLevel;
String nature;
BigDecimal recommendationRate;
Integer recommendationYear;
Boolean hasDoctorate;
Boolean hasMaster;
String department;
List<String> tags;
String famousUnion;
String imageUrl;
String introduction;
Integer status;

// 详细介绍（Tab2）
String address;
String admissionPhone;
String website;
Integer historyGroupScore;
Integer scienceGroupScore;
List<String> carouselImages;
String detailIntroduction;
RankingsVO rankings;
String abroadRate;
String genderRatio;
```

---

## 8. 实现任务清单

### 任务1：数据库表
- [ ] 创建 V5__create_universities_tables.sql

### 任务2：Entity和Mapper
- [ ] University.java
- [ ] UniversityDetail.java
- [ ] CampusGallery.java
- [ ] UniversityGuide.java
- [ ] 对应的4个Mapper

### 任务3：院校管理Controller/Service/DTO/VO
- [ ] UniversityController.java
- [ ] UniversityService.java + Impl
- [ ] UniversityQueryDTO/AddDTO/UpdateDTO
- [ ] UniversityDetailUpdateDTO
- [ ] UniversityListVO/DetailVO

### 任务4：校园图册Controller/Service/DTO/VO
- [ ] CampusGalleryController.java
- [ ] CampusGalleryService.java + Impl
- [ ] CampusGalleryQueryDTO/AddDTO/UpdateDTO
- [ ] CampusGalleryListVO

### 任务5：院校适应指南Controller/Service/DTO/VO
- [ ] UniversityGuideController.java
- [ ] UniversityGuideService.java + Impl
- [ ] UniversityGuideQueryDTO/AddDTO/UpdateDTO
- [ ] UniversityGuideListVO/DetailVO

### 任务6：Excel导入
- [ ] 添加EasyExcel依赖
- [ ] 配置文件上传大小限制
- [ ] StringArrayConverter.java
- [ ] UniversityExcelDTO.java
- [ ] UniversityDetailExcelDTO.java
- [ ] CampusGalleryExcelDTO.java
- [ ] UniversityGuideExcelDTO.java + GuideJsonbExcelDTO
- [ ] 4个导入接口实现

### 任务7：接口文档
- [ ] 创建 Products/order5.md
