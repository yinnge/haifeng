package com.haifeng.admin.service.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionUpdateDTO;
import com.haifeng.admin.vo.employment.industryPosition.finance.FinancePositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.finance.FinancePositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FinancePositionService {
    IPage<FinancePositionListVO> page(FinancePositionQueryDTO dto);
    FinancePositionDetailVO detail(Long id);
    void update(Long id, FinancePositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    void importExcel(MultipartFile file);
    String preValidate(MultipartFile file);
}
