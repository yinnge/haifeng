package com.haifeng.admin.service.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelQueryDTO;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelListVO;

import java.util.List;

public interface SafetyLevelService {
    IPage<SafetyLevelListVO> page(SafetyLevelQueryDTO dto);
    SafetyLevelDetailVO detail(Short level);
    void add(SafetyLevelAddDTO dto);
    void update(Short level, SafetyLevelAddDTO dto);
    void delete(Short level);
    void batchDelete(List<Short> levels);
}
