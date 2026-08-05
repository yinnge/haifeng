package com.haifeng.app.service.employment.contentManagement.examGuide;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import java.util.List;

public interface ExamGuideService {
    List<ExamGuideDetailVO> listByCategoryAndType(String guideCategory, String guideType);

    IPage<ExamGuideDetailVO> page(ExamGuideQueryDTO dto);

    ExamGuideDetailVO detail(Long id);
}
