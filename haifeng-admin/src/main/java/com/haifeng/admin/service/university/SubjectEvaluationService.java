package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.SubjectEvaluationAddDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationUpdateDTO;
import com.haifeng.admin.vo.university.SubjectEvaluationDetailVO;
import com.haifeng.admin.vo.university.SubjectEvaluationListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubjectEvaluationService {

    IPage<SubjectEvaluationListVO> page(SubjectEvaluationQueryDTO dto);

    SubjectEvaluationDetailVO detail(Long id);

    Long add(SubjectEvaluationAddDTO dto);

    void update(Long id, SubjectEvaluationUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    void hardDelete(Long id);

    void batchDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    void importSubjectEvaluations(MultipartFile file);
}
