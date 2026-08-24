package com.haifeng.app.service.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.vo.fileload.FileLoadDetailVO;
import com.haifeng.app.vo.fileload.FileLoadListVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;

import java.util.List;

public interface FileLoadService {

    IPage<FileLoadListVO> page(BasePageQueryDTO dto, String targetAudience,
                               String subject, String applicableStage, String tag);

    FileLoadDetailVO detail(Long id);

    String getPreviewUrl(Long id);

    String getDownloadUrl(Long id);

    /** 动态返回某受众下 applicable_stage 的去重值（用于前端按钮筛选） */
    List<String> listStages(String targetAudience);

    /** 动态返回某受众下 subject 的去重值（用于前端下拉筛选） */
    List<String> listSubjects(String targetAudience);

    /** 动态返回某受众下 tag 的去重值（用于前端下拉筛选） */
    List<String> listTags(String targetAudience);
}
