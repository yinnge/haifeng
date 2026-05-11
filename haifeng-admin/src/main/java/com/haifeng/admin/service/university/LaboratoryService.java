package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.LaboratoryAddDTO;
import com.haifeng.admin.dto.university.LaboratoryQueryDTO;
import com.haifeng.admin.dto.university.LaboratoryUpdateDTO;
import com.haifeng.admin.vo.university.LaboratoryDetailVO;
import com.haifeng.admin.vo.university.LaboratoryListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LaboratoryService {

    IPage<LaboratoryListVO> page(LaboratoryQueryDTO dto);

    LaboratoryDetailVO detail(Long id);

    Long add(LaboratoryAddDTO dto);

    void update(Long id, LaboratoryUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    void hardDelete(Long id);

    void batchDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    void importLaboratories(MultipartFile file);
}
