package com.haifeng.admin.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.SpecialChannelUnivAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.admin.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelUnivListVO;
import java.util.List;

public interface SpecialChannelUnivService {
    IPage<SpecialChannelUnivListVO> page(SpecialChannelUnivQueryDTO dto);
    SpecialChannelUnivDetailVO detail(Long id);
    void add(SpecialChannelUnivAddDTO dto);
    void update(Long id, SpecialChannelUnivAddDTO dto);
    void toggleActive(Long id);
    void delete(Long id);
    void batchDelete(List<Long> ids);
}
