package com.haifeng.admin.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.major.MajorPostgradDirectionAddDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionQueryDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionUpdateDTO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionDetailVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MajorPostgradDirectionService {

    /**
     * 分页查询列表
     */
    IPage<MajorPostgradDirectionListVO> list(MajorPostgradDirectionQueryDTO queryDTO);

    /**
     * 获取详情
     */
    MajorPostgradDirectionDetailVO getDetail(Long id);

    /**
     * 新增关联
     */
    void add(MajorPostgradDirectionAddDTO dto);

    /**
     * 修改关联
     */
    void update(Long id, MajorPostgradDirectionUpdateDTO dto);

    /**
     * 硬删除
     */
    void delete(Long id);

    /**
     * 批量硬删除
     */
    void batchDelete(List<Long> ids);

    /**
     * xlsx批量导入
     */
    ImportResultVO importData(MultipartFile file);
}
