# Model Providers Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add admin CRUD management for AI model provider API keys and make the PDF AI key pool load enabled DeepSeek records from the database.

**Architecture:** Store model provider records in `t_model_provider`, expose admin System-module CRUD endpoints, and reuse the same common entity/mapper from both admin and app modules. Replace the existing YAML-only `ApiKeyPool` key list with database-backed enabled DeepSeek provider configs while preserving user-based ordering and Redis cooldown fallback.

**Tech Stack:** Java 17, Spring Boot, MyBatis-Plus, PostgreSQL/Flyway SQL, RedisTemplate, JUnit 5, Mockito.

---

## File Structure

**Common module**
- Create `haifeng-common/src/main/java/com/haifeng/common/entity/system/ModelProvider.java` — MyBatis-Plus entity for `t_model_provider`.
- Create `haifeng-common/src/main/java/com/haifeng/common/mapper/system/ModelProviderMapper.java` — shared mapper with DeepSeek enabled provider query.
- Create `haifeng-common/src/main/java/com/haifeng/common/service/ai/dto/ModelProviderConfig.java` — runtime API key/model config used by `ApiKeyPool` and `AiChatServiceImpl`.
- Modify `haifeng-common/src/main/java/com/haifeng/common/service/ai/ApiKeyPool.java` — load enabled DeepSeek configs from DB, keep Redis cooldown.

**Admin module**
- Create `haifeng-admin/src/main/java/com/haifeng/admin/controller/system/ModelProviderController.java` — System-module REST endpoints.
- Create `haifeng-admin/src/main/java/com/haifeng/admin/service/system/ModelProviderService.java` — admin service interface.
- Create `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/system/ModelProviderServiceImpl.java` — CRUD/status implementation.
- Create DTOs under `haifeng-admin/src/main/java/com/haifeng/admin/dto/system/`:
  - `ModelProviderQueryDTO.java`
  - `ModelProviderCreateDTO.java`
  - `ModelProviderUpdateDTO.java`
  - `ModelProviderStatusDTO.java`
- Create VO `haifeng-admin/src/main/java/com/haifeng/admin/vo/system/ModelProviderVO.java`.

**App module**
- Modify `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImpl.java` — build request body with each provider config's `modelName`, call with each config's `apiKey`, and mark provider cooldown by id.

**Tests**
- Create `haifeng-admin/src/test/java/com/haifeng/admin/service/system/ModelProviderServiceImplTest.java`.
- Create or update `haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImplTest.java` for provider config fallback.
- Create or update `haifeng-common/src/test/java/com/haifeng/common/service/ai/ApiKeyPoolTest.java` if common tests are available; otherwise cover behavior through app service tests.

---

## Task 1: Common Entity, Mapper, and Runtime DTO

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/system/ModelProvider.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/system/ModelProviderMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/ai/dto/ModelProviderConfig.java`

- [ ] **Step 1: Add failing compile references in tests first**

Create tests that import `ModelProvider`, `ModelProviderMapper`, and `ModelProviderConfig` before production files exist. Expected failure: compilation errors for missing classes.

- [ ] **Step 2: Implement `ModelProvider` entity**

Use fields matching `t_model_provider`: `id`, `apiKey`, `modelName`, `providerName`, `status`, `createdAt`, `updatedAt`. Use `@TableName("t_model_provider")`, `@TableId(type = IdType.AUTO)`, Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.

- [ ] **Step 3: Implement mapper**

`ModelProviderMapper extends BaseMapper<ModelProvider>` and adds:

```java
@Select("SELECT id, api_key, model_name, provider_name, status, created_at, updated_at " +
        "FROM t_model_provider " +
        "WHERE provider_name = #{providerName} AND status = 1 " +
        "ORDER BY id ASC")
List<ModelProvider> findEnabledByProvider(@Param("providerName") String providerName);
```

- [ ] **Step 4: Implement runtime DTO**

`ModelProviderConfig` fields: `Long id`, `String apiKey`, `String modelName`, `String providerName` with Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.

---

## Task 2: Admin CRUD and Status APIs

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/system/ModelProviderQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/system/ModelProviderCreateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/system/ModelProviderUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/system/ModelProviderStatusDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/system/ModelProviderVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/system/ModelProviderService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/system/ModelProviderServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/system/ModelProviderController.java`
- Test: `haifeng-admin/src/test/java/com/haifeng/admin/service/system/ModelProviderServiceImplTest.java`

- [ ] **Step 1: Write failing service tests**

Cover these behaviors:
- `page()` supports fuzzy `providerName` and `modelName`, sorted by `createdAt DESC`.
- `detail(id)` throws `BusinessException(404, "模型供应商配置不存在")` when missing.
- `create(dto)` inserts a record with `status=1` by default when status is null.
- `update(id, dto)` updates only existing record and throws 404 when missing.
- `delete(id)` physically deletes by id and throws 404 when missing.
- `updateStatus(id, dto)` changes only `status` to 0 or 1 and throws 404 when missing.

- [ ] **Step 2: Implement DTO/VO classes**

`ModelProviderQueryDTO`: `page`, `size`, `providerName`, `modelName`, `status`.

`ModelProviderCreateDTO`: `apiKey`, `modelName`, `providerName`, `status` with validation; `apiKey`, `modelName`, `providerName` are `@NotBlank`; `status` can be null or 0/1.

`ModelProviderUpdateDTO`: same fields, required for full update except `status` can be null if unchanged.

`ModelProviderStatusDTO`: `@NotNull Integer status`, `@Min(0)`, `@Max(1)`.

`ModelProviderVO`: fields matching entity.

- [ ] **Step 3: Implement service interface**

Methods:

```java
IPage<ModelProviderVO> page(ModelProviderQueryDTO dto);
ModelProviderVO detail(Long id);
ModelProviderVO create(ModelProviderCreateDTO dto);
void update(Long id, ModelProviderUpdateDTO dto);
void delete(Long id);
void updateStatus(Long id, ModelProviderStatusDTO dto);
```

- [ ] **Step 4: Implement service**

Use `ModelProviderMapper`, `LambdaQueryWrapper`, `Page<>`, `StringUtils.hasText`, and `BeanUtils.copyProperties`. Use `modelProviderMapper.deleteById(id)` for physical delete. Do not add `is_deleted` logic.

- [ ] **Step 5: Implement controller**

Base path: `/api/v1/admin/system/model-providers`.

Endpoints:

```java
@GetMapping("/list")
@GetMapping("/{id}")
@PostMapping
@PutMapping("/{id}")
@DeleteMapping("/{id}")
@PatchMapping("/{id}/status")
```

Add `@OperationLog(module = "系统管理", action = "...")` on create/update/delete/status operations, following `SystemSettingsController` style.

---

## Task 3: Database-backed DeepSeek Key Pool

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/service/ai/ApiKeyPool.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImpl.java`
- Test: `haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImplTest.java`

- [ ] **Step 1: Write failing AI fallback tests**

Update the app AI service test to use `ModelProviderConfig` records:
- provider id 1 with key `key-1`, model `deepseek-chat`
- provider id 2 with key `key-2`, model `deepseek-chat`

Verify first-key failure calls second key and `keyPool.markUnhealthy(provider)` is called for id 1.

- [ ] **Step 2: Modify `ApiKeyPool`**

Inject `ModelProviderMapper`. Remove hard failure on empty YAML `deepseek.api-keys`. Add method:

```java
public List<ModelProviderConfig> orderedFallback(Long userId)
```

Implementation:
- Query `modelProviderMapper.findEnabledByProvider("deepseek")` each call so admin changes take effect without restart.
- Filter blank api keys/model names defensively.
- If empty, return empty list.
- Order by userId modulo size as current code does.
- Skip cooled-down providers in `pickProvider(Long userId)` if that method is retained.
- Use Redis cooldown key `ai:model-provider:cooldown:<id>`.

- [ ] **Step 3: Modify `AiChatServiceImpl`**

Change flow from `List<String> keys` to `List<ModelProviderConfig> providers`:
- If providers empty, return `Flux.error(new BusinessException(ResultCode.AI_ALL_KEYS_FAILED))`.
- Build request body per provider using provider's `modelName`.
- Call DeepSeek with provider's `apiKey`.
- On error, call `keyPool.markUnhealthy(provider)`.

Keep `DeepSeekProperties` for `maxTokens`, `temperature`, base URL config, timeout, and cooldown seconds. Do not use `properties.getModel()` for PDF AI request body once DB provider model is available.

---

## Task 4: Verification

**Files:**
- All files above.

- [ ] **Step 1: Run focused tests**

```bash
./mvnw -pl haifeng-admin -Dtest=ModelProviderServiceImplTest test
./mvnw -pl haifeng-app -Dtest=AiChatServiceImplTest test
```

Expected if unrelated current compile issues remain: compilation may fail in unrelated files such as existing PDF/civilService code. Record the first unrelated compile blocker exactly.

- [ ] **Step 2: Run whitespace check**

```bash
git diff --check -- haifeng-common haifeng-admin haifeng-app
```

Expected: no output and exit code 0.

- [ ] **Step 3: Manual endpoint smoke test after app starts**

Admin API examples:

```http
POST /api/v1/admin/system/model-providers
{
  "apiKey": "sk-test",
  "modelName": "deepseek-chat",
  "providerName": "deepseek",
  "status": 1
}

GET /api/v1/admin/system/model-providers/list?providerName=deep&modelName=chat&page=1&size=10
GET /api/v1/admin/system/model-providers/{id}
PATCH /api/v1/admin/system/model-providers/{id}/status
{"status":0}
DELETE /api/v1/admin/system/model-providers/{id}
```

App API smoke test:
- Insert at least two enabled `deepseek` records in `t_model_provider`.
- Call existing `POST /api/v1/app/algorithm/pdf/chat/stream`.
- Verify request uses DB `model_name` and rotates to another DB `api_key` when one fails.

---

## Self-Review

- Spec coverage: Admin CRUD, fuzzy provider/model query, physical delete, status disable/enable, and App PDF AI DB-backed DeepSeek key pool are all covered.
- Placeholder scan: no placeholders remain.
- Type consistency: `ModelProvider`, `ModelProviderConfig`, `ModelProviderMapper`, `ModelProviderService`, and `AiChatServiceImpl` method names are consistent across tasks.
