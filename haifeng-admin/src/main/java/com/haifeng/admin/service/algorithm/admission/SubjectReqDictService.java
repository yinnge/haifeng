package com.haifeng.admin.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictAddDTO;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictQueryDTO;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictDetailVO;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictListVO;

import java.util.List;

public interface SubjectReqDictService {
    IPage<SubjectReqDictListVO> page(SubjectReqDictQueryDTO dto);
    SubjectReqDictDetailVO detail(Integer id);
    Integer add(SubjectReqDictAddDTO dto);
    void update(Integer id, SubjectReqDictAddDTO dto);
    void delete(Integer id);
    void batchDelete(List<Integer> ids);
}
