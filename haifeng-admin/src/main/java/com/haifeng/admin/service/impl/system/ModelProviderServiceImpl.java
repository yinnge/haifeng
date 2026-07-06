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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
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
    public ModelProviderVO create(ModelProviderCreateDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
        String providerName = normalizeProviderName(dto.getProviderName());
        ModelProvider modelProvider = ModelProvider.builder()
                .apiKey(dto.getApiKey())
                .modelName(dto.getModelName())
                .providerName(providerName)
                .type(dto.getType())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        modelProviderMapper.insert(modelProvider);
        log.info("新增服务商配置成功: id={}, providerName={}, type={}, modelName={}",
                modelProvider.getId(), providerName, dto.getType(), dto.getModelName());
        return toVO(modelProvider);
    }

    @Override
    public void update(Long id, ModelProviderUpdateDTO dto) {
        ModelProvider modelProvider = getExisting(id);
        if (StringUtils.hasText(dto.getApiKey())) {
            modelProvider.setApiKey(dto.getApiKey());
        }
        String providerName = normalizeProviderName(dto.getProviderName());
        modelProvider.setModelName(dto.getModelName());
        modelProvider.setProviderName(providerName);
        if (StringUtils.hasText(dto.getType())) {
            modelProvider.setType(dto.getType());
        }
        if (dto.getDescription() != null) {
            modelProvider.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            modelProvider.setStatus(dto.getStatus());
        }
        modelProvider.setUpdatedAt(OffsetDateTime.now());

        modelProviderMapper.updateById(modelProvider);
        log.info("更新服务商配置成功: id={}, providerName={}, modelName={}", id, providerName, dto.getModelName());
    }

    @Override
    public void delete(Long id) {
        getExisting(id);
        modelProviderMapper.deleteById(id);
        log.info("删除服务商配置成功: id={}", id);
    }

    @Override
    public void updateStatus(Long id, ModelProviderStatusDTO dto) {
        ModelProvider modelProvider = getExisting(id);
        modelProvider.setStatus(dto.getStatus());
        modelProvider.setUpdatedAt(OffsetDateTime.now());

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
        ModelProviderVO vo = new ModelProviderVO();
        BeanUtils.copyProperties(modelProvider, vo);
        vo.setApiKeyMasked(maskApiKey(modelProvider.getApiKey()));
        return vo;
    }

    private String normalizeProviderName(String providerName) {
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
