package com.haifeng.admin.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.system.ModelProviderCreateDTO;
import com.haifeng.admin.dto.system.ModelProviderQueryDTO;
import com.haifeng.admin.dto.system.ModelProviderStatusDTO;
import com.haifeng.admin.dto.system.ModelProviderUpdateDTO;
import com.haifeng.admin.service.system.ModelProviderService;
import com.haifeng.admin.vo.system.ModelProviderVO;
import com.haifeng.common.entity.system.ModelProvider;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.system.ModelProviderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelProviderServiceImpl implements ModelProviderService {

    private static final String NOT_FOUND_MESSAGE = "服务商配置不存在";

    private final ModelProviderMapper modelProviderMapper;

    @Override
    public IPage<ModelProviderVO> page(ModelProviderQueryDTO dto) {
        Page<ModelProvider> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<ModelProvider> wrapper = new LambdaQueryWrapper<>();
        String providerName = trimToNull(dto.getProviderName());
        if (providerName != null) {
            wrapper.like(ModelProvider::getProviderName, providerName.toLowerCase(Locale.ROOT));
        }
        String modelName = trimToNull(dto.getModelName());
        if (modelName != null) {
            wrapper.like(ModelProvider::getModelName, modelName);
        }
        String type = trimToNull(dto.getType());
        if (type != null) {
            wrapper.eq(ModelProvider::getType, type);
        }
        if (dto.getStatus() != null) {
            wrapper.eq(ModelProvider::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(ModelProvider::getCreatedAt);

        IPage<ModelProvider> modelProviderPage = modelProviderMapper.selectPage(page, wrapper);
        return modelProviderPage.convert(this::toVO);
    }

    @Override
    public ModelProviderVO detail(Long id) {
        ModelProvider modelProvider = getExisting(id);
        return toVO(modelProvider);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelProviderVO create(ModelProviderCreateDTO dto) {
        ModelProvider modelProvider = ModelProvider.builder()
                .apiKey(dto.getApiKey())
                .baseUrl(dto.getBaseUrl())
                .modelName(dto.getModelName())
                .providerName(normalizeProviderName(dto.getProviderName()))
                .type(dto.getType())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        modelProviderMapper.insert(modelProvider);
        log.info("新增服务商配置成功: id={}, providerName={}, type={}, modelName={}",
                modelProvider.getId(), modelProvider.getProviderName(), dto.getType(), dto.getModelName());
        return toVO(modelProvider);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ModelProviderUpdateDTO dto) {
        ModelProvider modelProvider = getExisting(id);
        if (StringUtils.hasText(dto.getApiKey())) {
            modelProvider.setApiKey(dto.getApiKey());
        }
        if (dto.getBaseUrl() != null) {
            modelProvider.setBaseUrl(dto.getBaseUrl());
        }
        if (StringUtils.hasText(dto.getProviderName())) {
            modelProvider.setProviderName(normalizeProviderName(dto.getProviderName()));
        }
        if (StringUtils.hasText(dto.getModelName())) {
            modelProvider.setModelName(dto.getModelName());
        }
        if (StringUtils.hasText(dto.getType())) {
            modelProvider.setType(dto.getType());
        }
        if (dto.getDescription() != null) {
            modelProvider.setDescription(dto.getDescription());
        }

        modelProviderMapper.updateById(modelProvider);
        log.info("更新服务商配置成功: id={}, providerName={}, modelName={}", id, modelProvider.getProviderName(), modelProvider.getModelName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ModelProvider modelProvider = getExisting(id);
        modelProvider.setStatus(0);
        modelProviderMapper.updateById(modelProvider);
        log.info("禁用服务商配置成功: id={}, providerName={}", id, modelProvider.getProviderName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, ModelProviderStatusDTO dto) {
        ModelProvider modelProvider = getExisting(id);
        modelProvider.setStatus(dto.getStatus());

        modelProviderMapper.updateById(modelProvider);
        log.info("更新服务商配置状态成功: id={}, status={}", id, dto.getStatus());
    }

    private ModelProvider getExisting(Long id) {
        ModelProvider modelProvider = modelProviderMapper.selectById(id);
        if (modelProvider == null) {
            throw new BusinessException(404, NOT_FOUND_MESSAGE);
        }
        return modelProvider;
    }

    private ModelProviderVO toVO(ModelProvider modelProvider) {
        return ModelProviderVO.builder()
                .id(modelProvider.getId())
                .baseUrl(modelProvider.getBaseUrl())
                .modelName(modelProvider.getModelName())
                .providerName(modelProvider.getProviderName())
                .type(modelProvider.getType())
                .description(modelProvider.getDescription())
                .status(modelProvider.getStatus())
                .createdAt(modelProvider.getCreatedAt())
                .updatedAt(modelProvider.getUpdatedAt())
                .apiKeyMasked(maskApiKey(modelProvider.getApiKey()))
                .build();
    }

    private String normalizeProviderName(String providerName) {
        if (providerName == null) return null;
        return providerName.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String maskApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return null;
        }
        if (apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
