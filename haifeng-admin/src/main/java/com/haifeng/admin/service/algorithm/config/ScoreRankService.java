package com.haifeng.admin.service.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ScoreRankAddDTO;
import com.haifeng.admin.dto.algorithm.config.ScoreRankQueryDTO;
import com.haifeng.admin.vo.algorithm.config.ScoreRankDetailVO;
import com.haifeng.admin.vo.algorithm.config.ScoreRankListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ScoreRankService {
    IPage<ScoreRankListVO> page(ScoreRankQueryDTO dto);
    ScoreRankDetailVO detail(Long id);
    Long add(ScoreRankAddDTO dto);
    void update(Long id, ScoreRankAddDTO dto);
    void delete(Long id);
    void batchDelete(List<Long> ids);
    Integer importData(MultipartFile file);
}
