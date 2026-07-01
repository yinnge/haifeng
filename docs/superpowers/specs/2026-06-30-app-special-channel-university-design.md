# App端：特殊通道 + 院校通道关联 设计文档

## 范围
- haifeng-app 新增 `special` 模块（4个Controller），实现 14特殊通道.md 全部4个任务
- haifeng-app 在已有 `university` 模块追加 7院校管理.md 任务5（新增的通道-大学关联）

## 路由前缀
| 模块 | 路由 |
|------|------|
| 特殊通道 | `/api/v1/app/special/` |
| 院校关联 | `/api/v1/app/university/` |

## 模块结构

### special 模块（新建）

```
haifeng-app/src/main/java/com/haifeng/app/
├── controller/special/
│   ├── SpecialChannelController.java
│   ├── SpecialChannelUniversityController.java
│   ├── StrongBaseScoreController.java
│   └── StrongBaseUniversityController.java
├── service/special/
│   ├── SpecialChannelService.java
│   ├── SpecialChannelUniversityService.java
│   ├── StrongBaseScoreService.java
│   └── StrongBaseUniversityService.java
├── service/impl/special/
│   ├── SpecialChannelServiceImpl.java
│   ├── SpecialChannelUniversityServiceImpl.java
│   ├── StrongBaseScoreServiceImpl.java
│   └── StrongBaseUniversityServiceImpl.java
├── dto/special/
│   ├── SpecialChannelQueryDTO.java
│   ├── SpecialChannelUnivQueryDTO.java
│   └── StrongBaseScoreQueryDTO.java
├── dto/university/
│   └── UniversityChannelQueryDTO.java      ← 院校模块任务5用
└── vo/special/
    ├── SpecialChannelListVO.java
    ├── SpecialChannelDetailVO.java
    ├── SpecialChannelUnivListVO.java
    ├── SpecialChannelUnivDetailVO.java
    ├── StrongBaseScoreListVO.java
    ├── StrongBaseScoreDetailVO.java
    └── StrongBaseUniversityDetailVO.java
```

### university 模块（追加）

追加到现有 `UniversityController`/`UniversityService` 中：
- `GET /{universityId}/channels` → 大学关联的通道分页
- `GET /channel-options` → 通道下拉选项

## 数据结构对照

### 任务1：SpecialChannel（t_special_channel）
| 接口 | 方法 | 路径 | 权限 | 返回字段 |
|------|------|------|------|----------|
| 列表 | GET | `/channel/list` | 公开 | id,channelCode,channelName,subtitle,filterLabel,displayType |
| 详情 | GET | `/channel/{id}` | @RequireLogin | 列表字段+content |

筛选：displayType(精确), channelName(LIKE) — isActive=true

### 任务2：SpecialChannelUniversity（t_special_channel_university）
| 接口 | 方法 | 路径 | 权限 | 返回字段 |
|------|------|------|------|----------|
| 列表 | GET | `/channel-univ/list` | 公开 | universityId,universityName,year,regionTag,signupStart,signupEnd |
| 详情 | GET | `/channel-univ/{id}` | @RequireLogin | +officialUrl,brochureTitle,brochureContent |

筛选：channelCode(必传), channelName(LIKE), regionTag(精确,ProvinceEnum校验), signupStart(>=), signupEnd(<=) — isActive=true

### 任务3：StrongBaseScore（t_strong_base_score）
| 接口 | 方法 | 路径 | 权限 | 返回字段 |
|------|------|------|------|----------|
| 列表 | GET | `/strong-base-score/list` | 公开 | id,universityId,universityName,year,province,subjectType,majorName,majorCode,entryScore,entryScoreType,entryRatio,admissionScore,planCount,admissionCount |
| 详情 | GET | `/strong-base-score/{id}` | @RequireLogin | +entryFormula,admissionFormula,remark |

筛选：year,province,subjectType,entryScoreType(精确), universityName,majorName,majorCode(LIKE) — isActive=true

### 任务4：StrongBaseUniversity（t_strong_base_university）
| 接口 | 方法 | 路径 | 权限 | 返回字段 |
|------|------|------|------|----------|
| 详情 | GET | `/strong-base-univ/{universityId}` | @RequireLogin | id,universityId,universityName,isPilot,pilotYear,officialUrl,signupUrl,testBeforeScore,defaultEntryRatio,defaultAdmissionFormula,availableMajors,specialNotes |

### 院校任务5：SpecialChannelUniversity 另一视角
DTO: `UniversityChannelQueryDTO extends BasePageQueryDTO`（dto/university/）
VO: `UniversityChannelListVO` 和 `ChannelOptionVO`（vo/university/）

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 列表 | GET | `/university/{universityId}/channels` | 公开 | 分页查该大学的关联通道channelCode,channelName,year,regionTag,signupStart,signupEnd |
| 选项 | GET | `/university/channel-options` | 公开 | DISTINCT channelCode+channelName |

## 公共规范
- Mapper/Entity 全部在 haifeng-common 已存在，app端仅引用
- regionTag 用 String 传入，用 ProvinceEnum.isValid() 校验
- 所有查询默认 isActive=true 过滤
- 所有Controller用 @Validated + @RequiredArgsConstructor 模式
- Service层用 LambdaQueryWrapper 链式条件
