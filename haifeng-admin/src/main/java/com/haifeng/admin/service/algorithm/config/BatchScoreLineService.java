package com.haifeng.admin.service.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineAddDTO;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineQueryDTO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineDetailVO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface BatchScoreLineService {
    IPage<BatchScoreLineListVO> page(BatchScoreLineQueryDTO dto);
    BatchScoreLineDetailVO detail(Long id);
    Long add(BatchScoreLineAddDTO dto);
    void update(Long id, BatchScoreLineAddDTO dto);
    void delete(Long id);
    void batchDelete(List<Long> ids);
    void importData(MultipartFile file);
}
