package com.haifeng.app.service.employment.contentManagement.examGuide;

import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import java.util.List;

public interface ExamGuideService {
    List<ExamGuideDetailVO> listByCategoryAndType(String guideCategory, String guideType);
}
