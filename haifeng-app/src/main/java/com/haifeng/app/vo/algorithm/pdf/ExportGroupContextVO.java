package com.haifeng.app.vo.algorithm.pdf;

import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导出专业组上下文（PDF 三维度聚合共享）
 * <p>携带专业组定位信息及其可导出专业列表，供 university / city / major 三个维度聚合使用。
 * 仅含 is_exported=true 的专业；若可导出专业列表为空，该组在 {@code getExportGroupContexts} 阶段已被过滤。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportGroupContextVO {

    /** 专业组快照ID（t_wish_group_snapshot.id） */
    private Integer groupSnapshotId;

    /** 院校ID */
    private Long universityId;

    /** 城市名（唯一，用于 CityService.detailByName） */
    private String cityName;

    /** 专业组排序（group_sort_order） */
    private Integer groupSortOrder;

    /** 专业组代码 */
    private String groupCode;

    /** 专业组名称 */
    private String groupName;

    /** 该组下可导出的专业列表（is_exported=true，按 major_sort_order 升序） */
    private List<WishExportMajorVO> exportableMajors;
}
