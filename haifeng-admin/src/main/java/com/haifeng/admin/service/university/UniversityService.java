package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.*;
import com.haifeng.admin.vo.university.UniversityDetailVO;
import com.haifeng.admin.vo.university.UniversityListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 院校管理Service接口
 */
public interface UniversityService {

    /**
     * 分页查询院校列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    IPage<UniversityListVO> page(UniversityQueryDTO dto);

    /**
     * 获取院校详情（包含基础信息和详细介绍）
     *
     * @param id 院校ID
     * @return 院校详情
     */
    UniversityDetailVO detail(Long id);

    /**
     * 新增院校
     *
     * @param dto 院校信息
     * @return 新增的院校ID
     */
    Long add(UniversityAddDTO dto);

    /**
     * 修改院校基础信息
     *
     * @param id  院校ID
     * @param dto 修改内容
     */
    void update(Long id, UniversityUpdateDTO dto);

    /**
     * 修改院校详细介绍（Tab2）
     *
     * @param id  院校ID
     * @param dto 详情内容
     */
    void updateDetail(Long id, UniversityDetailUpdateDTO dto);

    /**
     * 修改院校状态（禁用/启用）
     *
     * @param id     院校ID
     * @param status 新状态（0禁用，1启用）
     */
    void updateStatus(Long id, Short status);

    /**
     * 软删除院校（可恢复，status=0）
     *
     * @param id 院校ID
     */
    void delete(Long id);

    /**
     * 硬删除院校（永久删除）
     *
     * @param id 院校ID
     */
    void hardDelete(Long id);

    /**
     * 批量软删除院校
     *
     * @param ids 院校ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 批量硬删除院校
     *
     * @param ids 院校ID列表
     */
    void batchHardDelete(List<Long> ids);

    /**
     * Excel导入院校主表数据
     *
     * @param file Excel文件
     */
    void importUniversities(MultipartFile file);

    /**
     * Excel导入院校详情表数据
     *
     * @param file Excel文件
     */
    void importUniversityDetails(MultipartFile file);
}
