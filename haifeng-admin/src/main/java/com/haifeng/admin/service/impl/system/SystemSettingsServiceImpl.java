package com.haifeng.admin.service.impl.system;

import com.haifeng.admin.dto.system.ProviderModelUpdateDTO;
import com.haifeng.admin.dto.system.SystemSettingsUpdateDTO;
import com.haifeng.admin.service.system.SystemSettingsService;
import com.haifeng.admin.vo.system.SystemSettingsVO;
import com.haifeng.common.entity.system.ModelProvider;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.system.ModelProviderMapper;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private static final Long SINGLETON_ID = 1L;

    private final SystemSettingsMapper settingsMapper;
    private final ModelProviderMapper modelProviderMapper;

    @Override
    public SystemSettingsVO get() {
        SystemSettings settings = settingsMapper.selectById(SINGLETON_ID);
        if (settings == null) {
            throw new BusinessException(404, "系统设置不存在");
        }

        SystemSettingsVO vo = new SystemSettingsVO();
        BeanUtils.copyProperties(settings, vo);
        return vo;
    }

    @Override
    public void update(SystemSettingsUpdateDTO dto) {
        SystemSettings settings = settingsMapper.selectById(SINGLETON_ID);
        if (settings == null) {
            throw new BusinessException(404, "系统设置不存在");
        }

        // 只更新非空字段
        if (dto.getSiteName() != null) {
            settings.setSiteName(dto.getSiteName());
        }
        if (dto.getSiteUrl() != null) {
            settings.setSiteUrl(dto.getSiteUrl());
        }
        if (dto.getSiteIcp() != null) {
            settings.setSiteIcp(dto.getSiteIcp());
        }
        if (dto.getSiteDescription() != null) {
            settings.setSiteDescription(dto.getSiteDescription());
        }
        if (dto.getApiNumber() != null) {
            settings.setApiNumber(dto.getApiNumber());
        }
        if (dto.getProPrice() != null) {
            settings.setProPrice(dto.getProPrice());
        }
        if (dto.getVipPrice() != null) {
            settings.setVipPrice(dto.getVipPrice());
        }
        if (dto.getSeoTitle() != null) {
            settings.setSeoTitle(dto.getSeoTitle());
        }
        if (dto.getSeoKeywords() != null) {
            settings.setSeoKeywords(dto.getSeoKeywords());
        }
        if (dto.getSeoDescription() != null) {
            settings.setSeoDescription(dto.getSeoDescription());
        }
        if (dto.getContactUrl() != null) {
            settings.setContactUrl(dto.getContactUrl());
        }
        if (dto.getBasicMessage() != null) {
            settings.setBasicMessage(dto.getBasicMessage());
        }

        settings.setUpdatedAt(OffsetDateTime.now());
        settingsMapper.updateById(settings);

        log.info("系统设置更新成功");
    }

    @Override
    public List<String> listProviders() {
        List<ModelProvider> providers = modelProviderMapper.selectList(null);
        return providers.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .map(ModelProvider::getProviderName)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void updateProviderModel(ProviderModelUpdateDTO dto) {
        // 验证 provider 是否存在且启用
        List<ModelProvider> providers = modelProviderMapper.findEnabledByProvider(dto.getProviderName());
        if (providers.isEmpty()) {
            throw new BusinessException(400, "所选服务商不存在或未启用");
        }

        // 验证 model 是否属于该 provider
        boolean modelExists = providers.stream()
                .anyMatch(p -> p.getModelName().equals(dto.getModelName()));
        if (!modelExists) {
            throw new BusinessException(400, "该模型不属于所选服务商");
        }

        // 更新系统设置
        SystemSettings settings = settingsMapper.selectById(SINGLETON_ID);
        if (settings == null) {
            throw new BusinessException(404, "系统设置不存在");
        }

        settings.setProviderName(dto.getProviderName());
        settings.setModelName(dto.getModelName());
        settings.setUpdatedAt(OffsetDateTime.now());
        settingsMapper.updateById(settings);

        log.info("系统设置服务商和模型更新成功: provider={}, model={}", dto.getProviderName(), dto.getModelName());
    }
}
