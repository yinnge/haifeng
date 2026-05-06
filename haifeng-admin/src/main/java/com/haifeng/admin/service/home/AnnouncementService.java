package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.AnnouncementAddDTO;
import com.haifeng.admin.dto.home.AnnouncementQueryDTO;
import com.haifeng.admin.dto.home.AnnouncementUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.vo.home.AnnouncementDetailVO;
import com.haifeng.admin.vo.home.AnnouncementListVO;

public interface AnnouncementService {

    /**
     * 分页查询公告列表
     */
    IPage<AnnouncementListVO> page(AnnouncementQueryDTO dto);

    /**
     * 获取公告详情
     */
    AnnouncementDetailVO detail(Long id);

    /**
     * 新增公告
     */
    Long add(AnnouncementAddDTO dto);

    /**
     * 更新公告
     */
    void update(Long id, AnnouncementUpdateDTO dto);

    /**
     * 更新公告状态
     */
    void updateStatus(Long id, StatusDTO dto);

    /**
     * 删除公告（软删除）
     */
    void delete(Long id);
}
