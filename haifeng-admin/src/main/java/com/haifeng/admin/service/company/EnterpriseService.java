package com.haifeng.admin.service.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.*;
import com.haifeng.admin.vo.company.EnterpriseDetailVO;
import com.haifeng.admin.vo.company.EnterpriseListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EnterpriseService {

    IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto);

    EnterpriseDetailVO detail(Long id);

    Long add(EnterpriseAddDTO dto);

    void update(Long id, EnterpriseUpdateDTO dto);

    void updateStatus(Long id, EnterpriseStatusDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void importEnterprises(MultipartFile file);
}
