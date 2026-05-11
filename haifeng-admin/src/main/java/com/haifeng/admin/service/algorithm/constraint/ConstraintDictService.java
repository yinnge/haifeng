package com.haifeng.admin.service.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictQueryDTO;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictListVO;
import java.util.List;

public interface ConstraintDictService {
    IPage<ConstraintDictListVO> page(ConstraintDictQueryDTO dto);
    ConstraintDictDetailVO detail(String code);
    void add(ConstraintDictAddDTO dto);
    void update(String code, ConstraintDictAddDTO dto);
    void toggleActive(String code);
    void delete(String code);
    void batchDelete(List<String> codes);
}
