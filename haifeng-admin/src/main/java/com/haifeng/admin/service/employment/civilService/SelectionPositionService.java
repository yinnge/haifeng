package com.haifeng.admin.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.SelectionPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.SelectionPositionUpdateDTO;
import com.haifeng.admin.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.SelectionPositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SelectionPositionService {
    IPage<SelectionPositionListVO> page(SelectionPositionQueryDTO dto);
    SelectionPositionDetailVO detail(Long id);
    void update(Long id, SelectionPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, String positionStatus);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    void importExcel(MultipartFile file);
}
