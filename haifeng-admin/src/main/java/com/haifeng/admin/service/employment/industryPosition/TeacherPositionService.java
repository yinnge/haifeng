package com.haifeng.admin.service.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionUpdateDTO;
import com.haifeng.admin.vo.employment.industryPosition.teacher.TeacherPositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.teacher.TeacherPositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeacherPositionService {
    IPage<TeacherPositionListVO> page(TeacherPositionQueryDTO dto);
    TeacherPositionDetailVO detail(Long id);
    void update(Long id, TeacherPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, String positionStatus);
    void batchDelete(List<Long> ids);
    void importExcel(MultipartFile file);
    String preValidate(MultipartFile file);
}
