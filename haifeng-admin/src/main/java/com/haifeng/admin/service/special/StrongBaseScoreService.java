package com.haifeng.admin.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.StrongBaseScoreAddDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.admin.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.admin.vo.special.StrongBaseScoreListVO;
import java.util.List;

public interface StrongBaseScoreService {
    IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto);
    StrongBaseScoreDetailVO detail(Long id);
    void add(StrongBaseScoreAddDTO dto);
    void update(Long id, StrongBaseScoreAddDTO dto);
    void toggleActive(Long id);
    void delete(Long id);
    void batchDelete(List<Long> ids);
}
