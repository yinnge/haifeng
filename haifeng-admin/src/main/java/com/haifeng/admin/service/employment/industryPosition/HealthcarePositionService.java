package com.haifeng.admin.service.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.healthcare.HealthcarePositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.healthcare.HealthcarePositionUpdateDTO;
import com.haifeng.admin.vo.employment.industryPosition.healthcare.HealthcarePositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.healthcare.HealthcarePositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HealthcarePositionService {
    IPage<HealthcarePositionListVO> page(HealthcarePositionQueryDTO dto);
    HealthcarePositionDetailVO detail(Long id);
    void update(Long id, HealthcarePositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, String positionStatus);
    void batchDelete(List<Long> ids);
    void importExcel(MultipartFile file);
    String preValidate(MultipartFile file);
}
