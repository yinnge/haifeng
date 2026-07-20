package com.haifeng.app.service.algorithm.wish;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.wish.WishGroupExportAllDTO;
import com.haifeng.app.dto.algorithm.wish.WishGroupSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorExportDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishPlanAddMajorsDTO;
import com.haifeng.app.vo.algorithm.pdf.ExportGroupContextVO;
import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanExportFileVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanExportProgressVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanGroupVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanListVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanMajorVO;
import com.haifeng.common.entity.algorithm.wish.WishGroupSnapshot;

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

    /**
     * 获取导出进度（SSE）
     *
     * @param planId 志愿方案ID
     * @return 导出进度VO
     */
    WishPlanExportProgressVO getExportProgress(Integer planId);

    /**
     * 生成导出文件（POST，非幂等）
     *
     * @param planId 志愿方案ID
     * @return 下载文件VO（包含 downloadUrl 指向 GET /download 端点）
     */
    WishPlanExportFileVO generateExportFile(Integer planId);

    /**
     * 读取已生成的导出文件（GET，只读）
     *
     * @param planId   志愿方案ID
     * @param fileName 文件名（已净化）
     * @return 文件字节数组
     */
    byte[] readExportFile(Integer planId, String fileName);

    /**
     * 保存导出状态到数据库
     *
     * @param planId 志愿方案ID
     */
    void saveExportStatusToDatabase(Integer planId);

    /**
     * 查询专业组快照（PDF导出用）
     * <p>用于获取该专业组关联的 university_id 与 city_name。
     *
     * @param groupSnapshotId 专业组快照ID
     * @return 专业组快照；不存在抛 {@code WISH_GROUP_NOT_FOUND}
     */
    WishGroupSnapshot getExportGroupSnapshot(Integer groupSnapshotId);

    /**
     * 查询专业组下可导出的专业列表（PDF导出用）
     * <p>仅返回 is_exported=true 的专业，按 major_sort_order 升序，已过滤空 major_id。
     * 每项携带 major_id、safety_level、level_short 及 history_scores 快照。
     *
     * @param groupSnapshotId 专业组快照ID
     * @return 可导出专业列表
     */
    List<WishExportMajorVO> getExportableMajors(Integer groupSnapshotId);

    /**
     * 取志愿表下所有"有可导出专业"的专业组上下文（PDF导出用）
     * <p>按 group_sort_order 升序；复用 {@link #getExportableMajors(Integer)}，
     * 若某专业组的可导出专业为空（即所有专业 is_exported=false）则被过滤，不返回。
     *
     * @param planId 志愿方案ID
     * @return 可导出的专业组上下文列表
     */
    List<ExportGroupContextVO> getExportGroupContexts(Integer planId);
}
