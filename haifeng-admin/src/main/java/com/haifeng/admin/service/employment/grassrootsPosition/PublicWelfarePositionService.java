package com.haifeng.admin.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionUpdateDTO;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PublicWelfarePositionService {
    IPage<PublicWelfarePositionListVO> page(PublicWelfarePositionQueryDTO dto);
    PublicWelfarePositionDetailVO detail(Long id);
    void update(Long id, PublicWelfarePositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    void importExcel(MultipartFile file);
}
