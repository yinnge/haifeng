package com.haifeng.admin.service.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.EnterprisePositionAddDTO;
import com.haifeng.admin.dto.company.EnterprisePositionQueryDTO;
import com.haifeng.admin.dto.company.EnterprisePositionUpdateDTO;
import com.haifeng.admin.vo.company.EnterprisePositionDetailVO;

import java.util.List;

public interface EnterprisePositionService {

    IPage<EnterprisePositionDetailVO> page(EnterprisePositionQueryDTO dto);

    EnterprisePositionDetailVO detail(Long id);

    Long add(Long enterpriseId, EnterprisePositionAddDTO dto);

    void update(Long id, EnterprisePositionUpdateDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
