package com.haifeng.admin.service.system;

import com.haifeng.admin.dto.system.SystemSettingsUpdateDTO;
import com.haifeng.admin.vo.system.SystemSettingsVO;

public interface SystemSettingsService {

    /**
     * 获取系统设置
     */
    SystemSettingsVO get();

    /**
     * 更新系统设置
     */
    void update(SystemSettingsUpdateDTO dto);
}
