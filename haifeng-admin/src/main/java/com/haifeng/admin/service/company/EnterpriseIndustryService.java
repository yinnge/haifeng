package com.haifeng.admin.service.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.EnterpriseIndustryAddDTO;
import com.haifeng.admin.dto.company.EnterpriseIndustryQueryDTO;
import com.haifeng.admin.vo.company.EnterpriseIndustryDetailVO;
import com.haifeng.admin.vo.company.EnterpriseIndustryListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EnterpriseIndustryService {

    IPage<EnterpriseIndustryListVO> page(EnterpriseIndustryQueryDTO dto);

    Long add(EnterpriseIndustryAddDTO dto);

    EnterpriseIndustryDetailVO detail(Long id);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void importEnterpriseIndustries(MultipartFile file);
}
