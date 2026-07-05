package com.haifeng.admin.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionUpdateDTO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GrassrootsProjectPositionService {
    IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionQueryDTO dto);
    GrassrootsProjectPositionDetailVO detail(Long id);
    void update(Long id, GrassrootsProjectPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    void importExcel(MultipartFile file);
}
