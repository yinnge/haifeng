package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.CampusGalleryAddDTO;
import com.haifeng.admin.dto.university.CampusGalleryQueryDTO;
import com.haifeng.admin.dto.university.CampusGalleryUpdateDTO;
import com.haifeng.admin.vo.university.CampusGalleryListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 校园图册Service接口
 */
public interface CampusGalleryService {

    /**
     * 分页查询校园图册列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    IPage<CampusGalleryListVO> page(CampusGalleryQueryDTO dto);

    /**
     * 新增校园图册
     *
     * @param dto 图册信息
     * @return 新增的图册ID
     */
    Long add(CampusGalleryAddDTO dto);

    /**
     * 修改校园图册
     *
     * @param id  图册ID
     * @param dto 修改内容
     */
    void update(Long id, CampusGalleryUpdateDTO dto);

    /**
     * 删除校园图册（软删除，status=0）
     *
     * @param id 图册ID
     */
    void delete(Long id);

    /**
     * 批量删除校园图册（软删除）
     *
     * @param ids 图册ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * Excel导入校园图册数据
     *
     * @param file Excel文件
     */
    void importGallery(MultipartFile file);
}
