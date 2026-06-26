package com.haifeng.app.service.employment.contentManagement.examGuide;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideListVO;
import java.util.List;

public interface ExamGuideService {
    IPage<ExamGuideListVO> page(ExamGuideQueryDTO dto);
    IPage<ExamGuideDetailVO> pageDetail(ExamGuideQueryDTO dto);
    ExamGuideDetailVO detail(Long id);
    List<ExamGuideDetailVO> listByCategoryAndType(String guideCategory, String guideType);
}
