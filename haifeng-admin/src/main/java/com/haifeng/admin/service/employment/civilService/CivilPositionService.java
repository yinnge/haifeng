package com.haifeng.admin.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.CivilPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.CivilPositionUpdateDTO;
import com.haifeng.admin.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.CivilPositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CivilPositionService {
    IPage<CivilPositionListVO> page(CivilPositionQueryDTO dto);
    CivilPositionDetailVO detail(Long id);
    void update(Long id, CivilPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    void importExcel(MultipartFile file);
}
