package com.haifeng.admin.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.SpecialChannelAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelQueryDTO;
import com.haifeng.admin.vo.special.SpecialChannelDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelListVO;
import com.haifeng.admin.vo.special.SpecialChannelOptionVO;
import java.util.List;

public interface SpecialChannelService {
    IPage<SpecialChannelListVO> page(SpecialChannelQueryDTO dto);
    SpecialChannelDetailVO detail(Long id);
    List<SpecialChannelOptionVO> listOptions();
    void add(SpecialChannelAddDTO dto);
    void update(Long id, SpecialChannelAddDTO dto);
    void toggleActive(Long id);
    void delete(Long id);
    void batchDelete(List<Long> ids);
}
