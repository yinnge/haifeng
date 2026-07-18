package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.DepartmentAddDTO;
import com.haifeng.admin.dto.university.DepartmentQueryDTO;
import com.haifeng.admin.dto.university.DepartmentUpdateDTO;
import com.haifeng.admin.vo.university.DepartmentDetailVO;
import com.haifeng.admin.vo.university.DepartmentListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DepartmentService {

    IPage<DepartmentListVO> page(DepartmentQueryDTO dto);

    DepartmentDetailVO detail(Long id);

    Long add(DepartmentAddDTO dto);

    void update(Long id, DepartmentUpdateDTO dto);

    void updateStatus(Long id, Short status);

    void delete(Long id);

    void hardDelete(Long id);

    void batchDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    void importDepartments(MultipartFile file);

    void importDepartmentReports(MultipartFile file);
}
