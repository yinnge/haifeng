package com.haifeng.admin.service.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.fileload.FileLoadQueryDTO;
import com.haifeng.admin.vo.fileload.FileLoadDetailVO;
import com.haifeng.admin.vo.fileload.FileLoadListVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileLoadService {

    Long upload(MultipartFile file, String targetAudience, String subject, String applicableStage, String createBy);

    IPage<FileLoadListVO> page(FileLoadQueryDTO dto, String targetAudience);

    FileLoadDetailVO detail(Long id);

    void update(Long id, String fileName, String subject, String applicableStage, Integer version, String updateBy);

    void delete(Long id);
}
