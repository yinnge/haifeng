package com.haifeng.admin.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.system.ModelProviderCreateDTO;
import com.haifeng.admin.dto.system.ModelProviderQueryDTO;
import com.haifeng.admin.dto.system.ModelProviderStatusDTO;
import com.haifeng.admin.dto.system.ModelProviderUpdateDTO;
import com.haifeng.admin.vo.system.ModelProviderVO;

public interface ModelProviderService {

    IPage<ModelProviderVO> page(ModelProviderQueryDTO dto);

    ModelProviderVO detail(Long id);

    ModelProviderVO create(ModelProviderCreateDTO dto);

    void update(Long id, ModelProviderUpdateDTO dto);

    void delete(Long id);

    void updateStatus(Long id, ModelProviderStatusDTO dto);
}
