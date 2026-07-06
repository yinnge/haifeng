package com.haifeng.admin.service.employment.contentManagement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideUpdateDTO;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExamGuideService {
    IPage<ExamGuideListVO> page(ExamGuideQueryDTO dto);
    ExamGuideDetailVO detail(Long id);
    void update(Long id, ExamGuideUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    void importExcel(MultipartFile file);
    String preValidate(MultipartFile file);
}
