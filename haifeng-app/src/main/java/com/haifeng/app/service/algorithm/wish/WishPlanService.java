package com.haifeng.app.service.algorithm.wish;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.wish.WishGroupExportAllDTO;
import com.haifeng.app.dto.algorithm.wish.WishGroupSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorExportDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishPlanAddMajorsDTO;
import com.haifeng.app.vo.algorithm.wish.WishPlanGroupVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanListVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanMajorVO;

import java.util.List;

/**
 * 志愿方案服务接口
 */
public interface WishPlanService {

    /**
     * 获取志愿方案默认数量限制
     * <p>从 system_settings 读取 5 个字段（搏/冲/稳/保/垫），
     * 通过 Redis 缓存 24 小时。
     *
     * @return 默认数量限制 VO，system_settings 为空时返回零值
     */
    WishPlanLimitVO getDefaultLimits();

    WishPlanListVO addMajors(WishPlanAddMajorsDTO dto);

    List<WishPlanListVO> myPlans();

    void deletePlan(Integer planId);

    IPage<WishPlanGroupVO> pageGroups(Integer planId, Integer page, Integer size);

    IPage<WishPlanMajorVO> pageMajors(Integer planId, Integer groupSnapshotId, Integer page, Integer size);

    /**
     * 修改专业组排序
     *
     * @param planId 志愿方案ID
     * @param dto    排序DTO
     */
    void updateGroupSortOrder(Integer planId, WishGroupSortDTO dto);

    /**
     * 修改专业排序
     *
     * @param planId         志愿方案ID
     * @param groupSnapshotId 专业组快照ID
     * @param dto            排序DTO
     */
    void updateMajorSortOrder(Integer planId, Integer groupSnapshotId, WishMajorSortDTO dto);

    /**
     * 修改专业导出状态（存Redis）
     *
     * @param planId   志愿方案ID
     * @param majorId  专业ID
     * @param dto      导出状态DTO
     */
    void updateMajorExportStatus(Integer planId, Integer majorId, WishMajorExportDTO dto);

    /**
     * 批量修改专业组下专业导出状态（存Redis）
     *
     * @param planId         志愿方案ID
     * @param groupSnapshotId 专业组快照ID
     * @param dto            导出状态DTO
     */
    void batchUpdateMajorExportStatus(Integer planId, Integer groupSnapshotId, WishGroupExportAllDTO dto);
}
