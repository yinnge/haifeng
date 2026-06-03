package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.CampusGalleryQueryDTO;
import com.haifeng.app.vo.university.CampusGalleryListVO;

public interface CampusGalleryService {

    /**
     * 按 universityId 分页查询校园图册（仅 status=1）
     * imageType 可选精准匹配，排序 sort_order ASC, id DESC
     */
    IPage<CampusGalleryListVO> page(Long universityId, CampusGalleryQueryDTO dto);
}
