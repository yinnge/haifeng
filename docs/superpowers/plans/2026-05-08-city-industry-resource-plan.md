# 城市、行业、资源管理模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现城市、行业、资源三个管理模块的完整CRUD功能和xlsx批量导入

**Architecture:** 遵循项目现有分层架构，Entity/Mapper在common模块，Controller/Service/DTO/VO在admin模块。xlsx导入复用LaboratoryServiceImpl的多Sheet解析模式。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, PostgreSQL (JSONB), 雪花算法ID

---

## 文件结构总览

### Flyway迁移
- Create: `haifeng-admin/src/main/resources/db/migration/V8__create_cities_industries.sql`

### Common模块 - Entity
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/city/City.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/city/CityDetail.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/industry/Industry.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/industry/IndustryDetail.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/resource/Resource.java`

### Common模块 - Mapper
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/city/CityMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/city/CityDetailMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/industry/IndustryMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/industry/IndustryDetailMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/resource/ResourceMapper.java`

### Admin模块 - 城市管理
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/city/CityController.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/city/CityService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/city/CityServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/city/CityQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/city/CityAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/city/CityUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/city/CityDetailUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/city/CityListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/city/CityDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/CityExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/CityDetailExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/` (14个JSONB ExcelDTO)

### Admin模块 - 行业管理
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/industry/IndustryController.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/industry/IndustryService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/industry/IndustryServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/IndustryQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/IndustryAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/IndustryUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/IndustryDetailUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/industry/IndustryListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/industry/IndustryDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/` (10个ExcelDTO)

### Admin模块 - 资源管理
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/resource/ResourceController.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/resource/ResourceService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/resource/ResourceServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/resource/ResourceQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/resource/ResourceAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/resource/ResourceUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/resource/ResourceListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/resource/ResourceDetailVO.java`

---

## Task 1: 创建Flyway迁移文件

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V8__create_cities_industries.sql`

- [ ] **Step 1: 创建V8迁移文件**

创建包含5个表的迁移SQL，使用BIGINT主键配合雪花算法。

- [ ] **Step 2: 验证SQL语法**

Run: `cd haifeng-admin && mvn flyway:validate -Dflyway.target=8`

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/resources/db/migration/V8__create_cities_industries.sql
git commit -m "feat(db): add V8 migration for city, industry, resource tables"
```

---

## Task 2: 创建Entity类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/city/City.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/city/CityDetail.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/industry/Industry.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/industry/IndustryDetail.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/resource/Resource.java`

- [ ] **Step 1: 创建City实体**
- [ ] **Step 2: 创建CityDetail实体（含JSONB字段用Map类型）**
- [ ] **Step 3: 创建Industry实体**
- [ ] **Step 4: 创建IndustryDetail实体**
- [ ] **Step 5: 创建Resource实体**
- [ ] **Step 6: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/city/
git add haifeng-common/src/main/java/com/haifeng/common/entity/industry/
git add haifeng-common/src/main/java/com/haifeng/common/entity/resource/
git commit -m "feat(entity): add City, CityDetail, Industry, IndustryDetail, Resource entities"
```

---

## Task 3: 创建Mapper接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/city/CityMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/city/CityDetailMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/industry/IndustryMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/industry/IndustryDetailMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/resource/ResourceMapper.java`

- [ ] **Step 1: 创建CityMapper（含existsByCityName方法）**
- [ ] **Step 2: 创建CityDetailMapper（含findByCityId方法）**
- [ ] **Step 3: 创建IndustryMapper（含existsByIndustryName方法）**
- [ ] **Step 4: 创建IndustryDetailMapper**
- [ ] **Step 5: 创建ResourceMapper**
- [ ] **Step 6: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/city/
git add haifeng-common/src/main/java/com/haifeng/common/mapper/industry/
git add haifeng-common/src/main/java/com/haifeng/common/mapper/resource/
git commit -m "feat(mapper): add mappers for city, industry, resource modules"
```

---

## Task 4: 创建资源管理模块（简单CRUD）

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/resource/ResourceQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/resource/ResourceAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/resource/ResourceUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/resource/ResourceListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/resource/ResourceDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/resource/ResourceService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/resource/ResourceServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/resource/ResourceController.java`

- [ ] **Step 1: 创建DTO类**
- [ ] **Step 2: 创建VO类**
- [ ] **Step 3: 创建Service接口**
- [ ] **Step 4: 创建ServiceImpl（参考现有模式）**
- [ ] **Step 5: 创建Controller**
- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/resource/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/resource/
git add haifeng-admin/src/main/java/com/haifeng/admin/service/resource/
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/resource/
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/resource/
git commit -m "feat(resource): add resource management module with CRUD"
```

---

## Task 5: 创建城市管理模块DTO/VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/city/CityQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/city/CityAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/city/CityUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/city/CityDetailUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/city/CityListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/city/CityDetailVO.java`

- [ ] **Step 1: 创建CityQueryDTO（继承BasePageQueryDTO）**
- [ ] **Step 2: 创建CityAddDTO**
- [ ] **Step 3: 创建CityUpdateDTO**
- [ ] **Step 4: 创建CityDetailUpdateDTO（含JSONB字段）**
- [ ] **Step 5: 创建CityListVO**
- [ ] **Step 6: 创建CityDetailVO（含主表+详情表所有字段）**
- [ ] **Step 7: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/city/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/city/
git commit -m "feat(city): add DTO and VO classes for city module"
```

---

## Task 6: 创建城市管理Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/city/CityService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/city/CityServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/city/CityController.java`

- [ ] **Step 1: 创建CityService接口**
- [ ] **Step 2: 创建CityServiceImpl（CRUD方法）**
- [ ] **Step 3: 创建CityController**
- [ ] **Step 4: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/city/
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/city/
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/city/
git commit -m "feat(city): add CityService and CityController with CRUD"
```

---

## Task 7: 创建城市xlsx导入功能

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/CityExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/CityDetailExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/IndustryStructureExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/HousingPriceLevelExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/HighEducationExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/BasicEducationExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/TransportationExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/EmploymentExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/EnterpriseStatsExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/FuturePlanExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/CultureExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/ConsumptionExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/MedicalExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/HousingPolicyExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/city/RentalCostExcelDTO.java`
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/city/CityServiceImpl.java`

- [ ] **Step 1: 创建CityExcelDTO（主表导入）**
- [ ] **Step 2: 创建CityDetailExcelDTO（详情基础字段）**
- [ ] **Step 3: 创建14个JSONB ExcelDTO**
- [ ] **Step 4: 在CityServiceImpl添加importCities方法**
- [ ] **Step 5: 在CityServiceImpl添加importCityDetails方法（多Sheet解析）**
- [ ] **Step 6: 在CityController添加导入接口**
- [ ] **Step 7: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/city/
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/city/CityServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/city/CityController.java
git commit -m "feat(city): add xlsx import for city and city detail"
```

---

## Task 8: 创建行业管理模块DTO/VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/IndustryQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/IndustryAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/IndustryUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/IndustryDetailUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/industry/IndustryListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/industry/IndustryDetailVO.java`

- [ ] **Step 1: 创建IndustryQueryDTO**
- [ ] **Step 2: 创建IndustryAddDTO**
- [ ] **Step 3: 创建IndustryUpdateDTO**
- [ ] **Step 4: 创建IndustryDetailUpdateDTO**
- [ ] **Step 5: 创建IndustryListVO**
- [ ] **Step 6: 创建IndustryDetailVO**
- [ ] **Step 7: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/industry/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/industry/
git commit -m "feat(industry): add DTO and VO classes for industry module"
```

---

## Task 9: 创建行业管理Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/industry/IndustryService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/industry/IndustryServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/industry/IndustryController.java`

- [ ] **Step 1: 创建IndustryService接口**
- [ ] **Step 2: 创建IndustryServiceImpl**
- [ ] **Step 3: 创建IndustryController**
- [ ] **Step 4: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/industry/
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/industry/
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/industry/
git commit -m "feat(industry): add IndustryService and IndustryController with CRUD"
```

---

## Task 10: 创建行业xlsx导入功能

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/IndustryExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/IndustryDetailExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/IndustryScaleExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/TalentDemandExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/IndustrySalaryExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/PolicyInfoExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/DevelopmentSupportExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/TalentAnalysisExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/TalentPolicyExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/SalaryDataExcelDTO.java`
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/industry/IndustryServiceImpl.java`

- [ ] **Step 1: 创建IndustryExcelDTO**
- [ ] **Step 2: 创建IndustryDetailExcelDTO**
- [ ] **Step 3: 创建8个JSONB ExcelDTO**
- [ ] **Step 4: 在IndustryServiceImpl添加importIndustries方法**
- [ ] **Step 5: 在IndustryServiceImpl添加importIndustryDetails方法**
- [ ] **Step 6: 在IndustryController添加导入接口**
- [ ] **Step 7: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/industry/
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/industry/IndustryServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/industry/IndustryController.java
git commit -m "feat(industry): add xlsx import for industry and industry detail"
```

---

## Task 11: 编译验证

- [ ] **Step 1: 编译整个项目**

Run: `mvn clean compile -DskipTests`

Expected: BUILD SUCCESS

- [ ] **Step 2: 修复编译错误（如有）**

- [ ] **Step 3: 最终Commit**

```bash
git add .
git commit -m "feat: complete city, industry, resource management modules"
```
