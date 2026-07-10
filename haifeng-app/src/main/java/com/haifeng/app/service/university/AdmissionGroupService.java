package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.AdmissionGroupQueryDTO;
import com.haifeng.app.vo.university.AdmissionGroupDetailVO;
import com.haifeng.app.vo.university.AdmissionGroupListVO;
import com.haifeng.app.vo.university.AdmissionMajorScoreListVO;

import java.util.List;

public interface AdmissionGroupService {

    IPage<AdmissionGroupListVO> pageByUniversity(Long universityId, AdmissionGroupQueryDTO dto);

    AdmissionGroupDetailVO getDetail(Long groupId);

    List<AdmissionMajorScoreListVO> listScores(Long groupId);
}
