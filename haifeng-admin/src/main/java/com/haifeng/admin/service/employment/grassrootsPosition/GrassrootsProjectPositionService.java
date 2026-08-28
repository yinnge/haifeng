package com.haifeng.admin.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionAddDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionUpdateDTO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GrassrootsProjectPositionService {
    IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionQueryDTO dto);
    GrassrootsProjectPositionDetailVO detail(Long id);
    void update(Long id, GrassrootsProjectPositionUpdateDTO dto);
    Long add(GrassrootsProjectPositionAddDTO dto);
    void delete(Long id);
    void updateStatus(Long id, String positionStatus);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    ImportResultVO importExcel(MultipartFile file);
}
