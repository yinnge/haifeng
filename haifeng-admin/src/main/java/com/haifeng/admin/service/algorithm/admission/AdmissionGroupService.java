package com.haifeng.admin.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdmissionGroupService {
    IPage<AdmissionGroupListVO> page(AdmissionGroupQueryDTO dto);
    AdmissionGroupDetailVO detail(Integer id);
    Integer add(AdmissionGroupAddDTO dto);
    void update(Integer id, AdmissionGroupAddDTO dto);
    void updateStatus(Integer id, Boolean isDeleted);
    void delete(Integer id);
    void batchDelete(List<Integer> ids);
    void importData(MultipartFile file);
    Integer recalcAll();
}
