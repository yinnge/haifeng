package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.*;
import com.haifeng.admin.vo.home.*;

public interface InstitutionService {
    IPage<InstitutionListVO> page(InstitutionQueryDTO dto);
    InstitutionDetailVO detail(Long id);
    Long add(InstitutionAddDTO dto);
    void update(Long id, InstitutionUpdateDTO dto);
    void updateStatus(Long id, StatusDTO dto);
    /**
     * 硬删除培训机构
     */
    void delete(Long id);
}
