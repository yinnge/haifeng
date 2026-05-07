package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.UniversityGuideAddDTO;
import com.haifeng.admin.dto.university.UniversityGuideQueryDTO;
import com.haifeng.admin.dto.university.UniversityGuideUpdateDTO;
import com.haifeng.admin.vo.university.UniversityGuideDetailVO;
import com.haifeng.admin.vo.university.UniversityGuideListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 院校适应指南Service接口
 */
public interface UniversityGuideService {

    /**
     * 分页查询院校适应指南列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    IPage<UniversityGuideListVO> page(UniversityGuideQueryDTO dto);

    /**
     * 获取院校适应指南详情
     *
     * @param id 指南ID
     * @return 指南详情
     */
    UniversityGuideDetailVO detail(Long id);

    /**
     * 新增院校适应指南
     *
     * @param dto 指南信息
     * @return 新增的指南ID
     */
    Long add(UniversityGuideAddDTO dto);

    /**
     * 修改院校适应指南
     *
     * @param id  指南ID
     * @param dto 修改内容
     */
    void update(Long id, UniversityGuideUpdateDTO dto);

    /**
     * 删除院校适应指南（软删除，status=0）
     *
     * @param id 指南ID
     */
    void delete(Long id);

    /**
     * 批量删除院校适应指南（软删除）
     *
     * @param ids 指南ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * Excel导入院校适应指南数据
     *
     * @param file Excel文件
     */
    void importGuide(MultipartFile file);
}
