package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.CommissionQueryDTO;
import com.haifeng.admin.vo.user.CommissionListVO;

public interface CommissionService {

    IPage<CommissionListVO> page(CommissionQueryDTO dto);

    void delete(Long id);

    void hardDelete(Long id);

    void restore(Long id);
}
