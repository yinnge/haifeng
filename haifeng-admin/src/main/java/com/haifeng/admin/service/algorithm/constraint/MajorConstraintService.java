package com.haifeng.admin.service.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintQueryDTO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MajorConstraintService {
    IPage<MajorConstraintListVO> page(MajorConstraintQueryDTO dto);
    MajorConstraintDetailVO detail(Long id);
    Long add(MajorConstraintAddDTO dto);
    void delete(Long id);
    void batchDelete(List<Long> ids);
    void importData(MultipartFile file);
}
