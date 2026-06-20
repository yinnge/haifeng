package com.haifeng.app.service.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.industryPosition.TeacherPositionSearchDTO;
import com.haifeng.app.vo.employment.industryPosition.TeacherPositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.TeacherPositionListVO;

public interface TeacherPositionService {

    IPage<TeacherPositionListVO> page(TeacherPositionSearchDTO dto);

    TeacherPositionDetailVO detail(Long id);
}
