package com.haifeng.app.service.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.vo.fileload.FileLoadDetailVO;
import com.haifeng.app.vo.fileload.FileLoadListVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;

public interface FileLoadService {

    IPage<FileLoadListVO> page(BasePageQueryDTO dto, String targetAudience, String subject, String applicableStage);

    FileLoadDetailVO detail(Long id);

    String getPreviewUrl(Long id);

    String getDownloadUrl(Long id);
}
