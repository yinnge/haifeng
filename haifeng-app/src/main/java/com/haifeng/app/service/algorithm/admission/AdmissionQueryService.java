package com.haifeng.app.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.app.dto.algorithm.admission.AdmissionMajorQueryDTO;
import com.haifeng.app.vo.algorithm.admission.AdmissionGroupPageVO;
import com.haifeng.app.vo.algorithm.admission.AdmissionMajorPageVO;

public interface AdmissionQueryService {

    /**
     * 分页查询专业组
     */
    IPage<AdmissionGroupPageVO> pageGroups(AdmissionGroupQueryDTO dto);

    /**
     * 分页查询专业明细
     */
    IPage<AdmissionMajorPageVO> pageMajors(AdmissionMajorQueryDTO dto);
}
