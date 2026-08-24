package com.haifeng.admin.service.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.fileload.FileLoadQueryDTO;
import com.haifeng.admin.vo.fileload.FileLoadDetailVO;
import com.haifeng.admin.vo.fileload.FileLoadListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileLoadService {

    Long upload(MultipartFile file, String targetAudience, String subject, String applicableStage,
                String description, String tag, String createBy);

    IPage<FileLoadListVO> page(FileLoadQueryDTO dto, String targetAudience);

    FileLoadDetailVO detail(Long id);

    void update(Long id, String fileName, String subject, String applicableStage,
                 String description, String tag, Integer version, String updateBy);

    void delete(Long id);

    /** 动态返回某受众下 applicable_stage 的去重值（前端筛选下拉，不再写死） */
    List<String> listStages(String targetAudience);

    /** 动态返回某受众下 subject 的去重值 */
    List<String> listSubjects(String targetAudience);

    /** 动态返回某受众下 tag 的去重值 */
    List<String> listTags(String targetAudience);
}
