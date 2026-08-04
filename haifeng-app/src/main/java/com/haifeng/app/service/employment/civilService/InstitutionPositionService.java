package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO;

import java.util.Map;

public interface InstitutionPositionService {

    IPage<InstitutionPositionListVO> page(InstitutionPositionSearchDTO dto);

    InstitutionPositionDetailVO detail(Long id);

    /**
     * 获取动态筛选选项（考试类别、职位类型、特殊岗位等去重值列表）
     */
    Map<String, Object> getFilters();
}
