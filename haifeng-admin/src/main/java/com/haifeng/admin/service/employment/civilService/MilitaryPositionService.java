package com.haifeng.admin.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionAddDTO;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionUpdateDTO;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MilitaryPositionService {
    IPage<MilitaryPositionListVO> page(MilitaryPositionQueryDTO dto);
    MilitaryPositionDetailVO detail(Long id);
    Long add(MilitaryPositionAddDTO dto);
    void update(Long id, MilitaryPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, String positionStatus);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    ImportResultVO importExcel(MultipartFile file);
}
