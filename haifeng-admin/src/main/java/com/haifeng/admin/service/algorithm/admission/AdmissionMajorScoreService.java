package com.haifeng.admin.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreQueryDTO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreListVO;

import java.util.List;

public interface AdmissionMajorScoreService {
    IPage<AdmissionMajorScoreListVO> page(AdmissionMajorScoreQueryDTO dto);
    AdmissionMajorScoreDetailVO detail(Integer id);
    Integer add(AdmissionMajorScoreAddDTO dto);
    void update(Integer id, AdmissionMajorScoreAddDTO dto);
    void updateStatus(Integer id, Boolean isDeleted);
    void delete(Integer id);
    void batchDelete(List<Integer> ids);
}
