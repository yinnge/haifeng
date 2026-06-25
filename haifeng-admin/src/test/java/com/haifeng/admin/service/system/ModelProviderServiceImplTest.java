package com.haifeng.admin.service.system;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.system.ModelProviderCreateDTO;
import com.haifeng.admin.dto.system.ModelProviderQueryDTO;
import com.haifeng.admin.dto.system.ModelProviderStatusDTO;
import com.haifeng.admin.dto.system.ModelProviderUpdateDTO;
import com.haifeng.admin.service.impl.system.ModelProviderServiceImpl;
import com.haifeng.admin.vo.system.ModelProviderVO;
import com.haifeng.common.entity.system.ModelProvider;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.system.ModelProviderMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelProviderServiceImplTest {

    @Mock
    private ModelProviderMapper modelProviderMapper;

    @InjectMocks
    private ModelProviderServiceImpl modelProviderService;

    @Captor
    private ArgumentCaptor<Page<ModelProvider>> pageCaptor;

    @Captor
    private ArgumentCaptor<LambdaQueryWrapper<ModelProvider>> wrapperCaptor;

    @Captor
    private ArgumentCaptor<ModelProvider> modelProviderCaptor;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ModelProvider.class);
    }

    @BeforeEach
    void setUp() {
        modelProviderService = new ModelProviderServiceImpl(modelProviderMapper);
    }

    @Test
    void pageBuildsFuzzyFiltersStatusAndCreatedAtDescOrder() {
        ModelProviderQueryDTO dto = new ModelProviderQueryDTO();
        dto.setPage(2);
        dto.setSize(10);
        dto.setProviderName(" Deep ");
        dto.setModelName("chat");
        dto.setStatus(1);

        ModelProvider provider = ModelProvider.builder()
                .id(1L)
                .apiKey("sk-test")
                .providerName("deepseek")
                .modelName("deepseek-chat")
                .status(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        Page<ModelProvider> mapperPage = new Page<>(2, 10, 1);
        mapperPage.setRecords(List.of(provider));
        when(modelProviderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        IPage<ModelProviderVO> result = modelProviderService.page(dto);

        verify(modelProviderMapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(10);
        assertThat(wrapperCaptor.getValue().getCustomSqlSegment())
                .contains("provider_name LIKE")
                .contains("model_name LIKE")
                .contains("status =")
                .contains("ORDER BY created_at DESC");
        assertThat(wrapperCaptor.getValue().getParamNameValuePairs().values())
                .contains("%deep%", "%chat%", 1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getApiKeyMasked()).isEqualTo("****");
    }

    @Test
    void detailReturnsMaskedApiKeyWithoutRawKey() {
        ModelProvider existing = ModelProvider.builder()
                .id(9L)
                .apiKey("sk-1234567890")
                .modelName("deepseek-chat")
                .providerName("deepseek")
                .status(1)
                .build();
        when(modelProviderMapper.selectById(9L)).thenReturn(existing);

        ModelProviderVO result = modelProviderService.detail(9L);

        assertThat(result.getApiKeyMasked()).isEqualTo("sk-1****7890");
        assertThat(ModelProviderVO.class.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("apiKey")
                .contains("apiKeyMasked");
    }

    @Test
    void detailMasksBlankApiKeyAsNull() {
        ModelProvider existing = ModelProvider.builder()
                .id(11L)
                .apiKey("   ")
                .modelName("deepseek-chat")
                .providerName("deepseek")
                .status(1)
                .build();
        when(modelProviderMapper.selectById(11L)).thenReturn(existing);

        ModelProviderVO result = modelProviderService.detail(11L);

        assertThat(result.getApiKeyMasked()).isNull();
    }

    @Test
    void detailThrowsWhenMissing() {
        when(modelProviderMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> modelProviderService.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("模型供应商配置不存在")
                .extracting("code")
                .isEqualTo(404);
    }

    @Test
    void createDefaultsStatusToEnabledWhenStatusNull() {
        ModelProviderCreateDTO dto = new ModelProviderCreateDTO();
        dto.setApiKey("sk-create");
        dto.setModelName("deepseek-chat");
        dto.setProviderName(" DeepSeek ");

        when(modelProviderMapper.insert(any(ModelProvider.class))).thenAnswer(invocation -> {
            ModelProvider provider = invocation.getArgument(0);
            provider.setId(10L);
            return 1;
        });

        ModelProviderVO result = modelProviderService.create(dto);

        verify(modelProviderMapper).insert(modelProviderCaptor.capture());
        ModelProvider inserted = modelProviderCaptor.getValue();
        assertThat(inserted.getProviderName()).isEqualTo("deepseek");
        assertThat(inserted.getStatus()).isEqualTo(1);
        assertThat(inserted.getCreatedAt()).isNotNull();
        assertThat(inserted.getUpdatedAt()).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getApiKeyMasked()).isEqualTo("sk-c****eate");
    }

    @Test
    void updateThrowsWhenMissing() {
        ModelProviderUpdateDTO dto = updateDto();
        when(modelProviderMapper.selectById(88L)).thenReturn(null);

        assertThatThrownBy(() -> modelProviderService.update(88L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("模型供应商配置不存在")
                .extracting("code")
                .isEqualTo(404);
        verify(modelProviderMapper, never()).updateById(any(ModelProvider.class));
    }

    @Test
    void updateExistingRecord() {
        OffsetDateTime originalUpdatedAt = OffsetDateTime.now().minusDays(1);
        ModelProvider existing = ModelProvider.builder()
                .id(5L)
                .apiKey("old-key")
                .modelName("old-model")
                .providerName("old-provider")
                .status(1)
                .updatedAt(originalUpdatedAt)
                .build();
        ModelProviderUpdateDTO dto = updateDto();
        dto.setStatus(0);
        when(modelProviderMapper.selectById(5L)).thenReturn(existing);

        modelProviderService.update(5L, dto);

        verify(modelProviderMapper).updateById(modelProviderCaptor.capture());
        ModelProvider updated = modelProviderCaptor.getValue();
        assertThat(updated.getApiKey()).isEqualTo("sk-update");
        assertThat(updated.getModelName()).isEqualTo("deepseek-reasoner");
        assertThat(updated.getProviderName()).isEqualTo("deepseek");
        assertThat(updated.getStatus()).isEqualTo(0);
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void updateKeepsStatusWhenStatusNull() {
        ModelProvider existing = ModelProvider.builder()
                .id(6L)
                .apiKey("old-key")
                .modelName("old-model")
                .providerName("old-provider")
                .status(1)
                .build();
        ModelProviderUpdateDTO dto = updateDto();
        dto.setStatus(null);
        when(modelProviderMapper.selectById(6L)).thenReturn(existing);

        modelProviderService.update(6L, dto);

        verify(modelProviderMapper).updateById(modelProviderCaptor.capture());
        assertThat(modelProviderCaptor.getValue().getStatus()).isEqualTo(1);
    }

    @Test
    void updateKeepsExistingApiKeyWhenApiKeyNull() {
        ModelProvider existing = ModelProvider.builder()
                .id(12L)
                .apiKey("old-secret")
                .modelName("old-model")
                .providerName("old-provider")
                .status(1)
                .build();
        ModelProviderUpdateDTO dto = updateDto();
        dto.setApiKey(null);
        when(modelProviderMapper.selectById(12L)).thenReturn(existing);

        modelProviderService.update(12L, dto);

        verify(modelProviderMapper).updateById(modelProviderCaptor.capture());
        assertThat(modelProviderCaptor.getValue().getApiKey()).isEqualTo("old-secret");
        assertThat(modelProviderCaptor.getValue().getModelName()).isEqualTo("deepseek-reasoner");
        assertThat(modelProviderCaptor.getValue().getProviderName()).isEqualTo("deepseek");
    }

    @Test
    void updateKeepsExistingApiKeyWhenApiKeyBlank() {
        ModelProvider existing = ModelProvider.builder()
                .id(13L)
                .apiKey("old-secret")
                .modelName("old-model")
                .providerName("old-provider")
                .status(1)
                .build();
        ModelProviderUpdateDTO dto = updateDto();
        dto.setApiKey("   ");
        when(modelProviderMapper.selectById(13L)).thenReturn(existing);

        modelProviderService.update(13L, dto);

        verify(modelProviderMapper).updateById(modelProviderCaptor.capture());
        assertThat(modelProviderCaptor.getValue().getApiKey()).isEqualTo("old-secret");
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(modelProviderMapper.selectById(77L)).thenReturn(null);

        assertThatThrownBy(() -> modelProviderService.delete(77L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("模型供应商配置不存在")
                .extracting("code")
                .isEqualTo(404);
        verify(modelProviderMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void deletePhysicallyDeletesById() {
        ModelProvider existing = ModelProvider.builder().id(7L).build();
        when(modelProviderMapper.selectById(7L)).thenReturn(existing);

        modelProviderService.delete(7L);

        verify(modelProviderMapper).deleteById(eq(7L));
    }

    @Test
    void updateStatusThrowsWhenMissing() {
        ModelProviderStatusDTO dto = statusDto(1);
        when(modelProviderMapper.selectById(66L)).thenReturn(null);

        assertThatThrownBy(() -> modelProviderService.updateStatus(66L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("模型供应商配置不存在")
                .extracting("code")
                .isEqualTo(404);
        verify(modelProviderMapper, never()).updateById(any(ModelProvider.class));
    }

    @Test
    void updateStatusChangesStatusOnly() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(2);
        OffsetDateTime originalUpdatedAt = OffsetDateTime.now().minusDays(1);
        ModelProvider existing = ModelProvider.builder()
                .id(8L)
                .apiKey("sk-status")
                .modelName("deepseek-chat")
                .providerName("deepseek")
                .status(1)
                .createdAt(createdAt)
                .updatedAt(originalUpdatedAt)
                .build();
        when(modelProviderMapper.selectById(8L)).thenReturn(existing);

        modelProviderService.updateStatus(8L, statusDto(0));

        verify(modelProviderMapper).updateById(modelProviderCaptor.capture());
        ModelProvider updated = modelProviderCaptor.getValue();
        assertThat(updated.getApiKey()).isEqualTo("sk-status");
        assertThat(updated.getModelName()).isEqualTo("deepseek-chat");
        assertThat(updated.getProviderName()).isEqualTo("deepseek");
        assertThat(updated.getStatus()).isEqualTo(0);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    private ModelProviderUpdateDTO updateDto() {
        ModelProviderUpdateDTO dto = new ModelProviderUpdateDTO();
        dto.setApiKey("sk-update");
        dto.setModelName("deepseek-reasoner");
        dto.setProviderName(" DeepSeek ");
        return dto;
    }

    private ModelProviderStatusDTO statusDto(Integer status) {
        ModelProviderStatusDTO dto = new ModelProviderStatusDTO();
        dto.setStatus(status);
        return dto;
    }
}
