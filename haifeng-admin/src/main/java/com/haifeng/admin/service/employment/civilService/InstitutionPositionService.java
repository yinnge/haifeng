package com.haifeng.admin.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.InstitutionPositionAddDTO;
import com.haifeng.admin.dto.employment.civilService.InstitutionPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.InstitutionPositionUpdateDTO;
import com.haifeng.admin.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.InstitutionPositionListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InstitutionPositionService {
    IPage<InstitutionPositionListVO> page(InstitutionPositionQueryDTO dto);
    InstitutionPositionDetailVO detail(Long id);
    Long add(InstitutionPositionAddDTO dto);
    void update(Long id, InstitutionPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    ImportResultVO importExcel(MultipartFile file);
}
