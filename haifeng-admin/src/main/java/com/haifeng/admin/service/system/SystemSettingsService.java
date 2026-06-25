package com.haifeng.admin.service.system;

import com.haifeng.admin.dto.system.ProviderModelUpdateDTO;
import com.haifeng.admin.dto.system.SystemSettingsUpdateDTO;
import com.haifeng.admin.vo.system.SystemSettingsVO;

import java.util.List;

public interface SystemSettingsService {

    /**
     * 获取系统设置
     */
    SystemSettingsVO get();

    /**
     * 更新系统设置
     */
    void update(SystemSettingsUpdateDTO dto);

    /**
     * 获取所有启用的服务商列表（去重）
     */
    List<String> listProviders();

    /**
     * 更新系统设置中的服务商和模型
     */
    void updateProviderModel(ProviderModelUpdateDTO dto);
}
