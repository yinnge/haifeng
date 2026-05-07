package com.haifeng.admin.service.major;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorUniversityListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PostgradMajorUniversityService {
    Page<PostgradMajorUniversityListVO> list(PostgradMajorUniversityQueryDTO queryDTO);
    void softDelete(Long id);
    void hardDelete(Long id);
    void batchSoftDelete(List<Long> ids);
    void batchHardDelete(List<Long> ids);
    ImportResultVO importPostgradMajorUniversity(MultipartFile file);
    void restore(Long id);
}
