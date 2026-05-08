package com.haifeng.admin.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.major.*;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorDetailVO;
import com.haifeng.admin.vo.major.MajorListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MajorService {
    IPage<MajorListVO> list(MajorQueryDTO queryDTO);
    MajorDetailVO getById(Long id);
    Long add(MajorAddDTO addDTO);
    void update(Long id, MajorUpdateDTO updateDTO);
    void updateStatus(Long id, Short status);
    void softDelete(Long id);
    void hardDelete(Long id);
    void batchSoftDelete(List<Long> ids);
    void batchHardDelete(List<Long> ids);
    void updateDetail(Long id, MajorDetailUpdateDTO detailDTO);
    ImportResultVO importMajor(MultipartFile file);
    ImportResultVO importMajorDetail(MultipartFile file);
    void restore(Long id);
}
