package com.haifeng.admin.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.StrongBaseUnivAddDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivQueryDTO;
import com.haifeng.admin.vo.special.StrongBaseUnivDetailVO;
import com.haifeng.admin.vo.special.StrongBaseUnivListVO;
import java.util.List;

public interface StrongBaseUnivService {
    IPage<StrongBaseUnivListVO> page(StrongBaseUnivQueryDTO dto);
    StrongBaseUnivDetailVO detail(Long id);
    void add(StrongBaseUnivAddDTO dto);
    void update(Long id, StrongBaseUnivAddDTO dto);
    void delete(Long id);
    void batchDelete(List<Long> ids);
}
