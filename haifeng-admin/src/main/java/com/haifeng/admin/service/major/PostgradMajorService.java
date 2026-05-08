package com.haifeng.admin.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.major.*;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorDetailVO;
import com.haifeng.admin.vo.major.PostgradMajorListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PostgradMajorService {
    IPage<PostgradMajorListVO> list(PostgradMajorQueryDTO queryDTO);
    PostgradMajorDetailVO getById(Long id);
    Long add(PostgradMajorAddDTO addDTO);
    void update(Long id, PostgradMajorUpdateDTO updateDTO);
    void updateStatus(Long id, Short status);
    void softDelete(Long id);
    void hardDelete(Long id);
    void batchSoftDelete(List<Long> ids);
    void batchHardDelete(List<Long> ids);
    ImportResultVO importPostgradMajor(MultipartFile file);
    void restore(Long id);
}
