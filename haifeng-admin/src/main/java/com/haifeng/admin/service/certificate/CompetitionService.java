package com.haifeng.admin.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;

import java.util.List;

public interface CompetitionService {

    /**
     * 分页查询竞赛列表
     */
    IPage<CompetitionListVO> listCompetitions(CompetitionQueryDTO queryDTO);

    /**
     * 获取竞赛详情（包含详情表数据）
     */
    CompetitionDetailVO getCompetitionDetail(Long id);

    /**
     * 新增竞赛（同时创建详情记录）
     */
    Long addCompetition(CompetitionAddDTO addDTO);

    /**
     * 更新竞赛（同时更新详情记录）
     */
    void updateCompetition(CompetitionUpdateDTO updateDTO);

    /**
     * 软删除竞赛（同时软删除详情）
     */
    void softDeleteCompetition(Long id);

    /**
     * 硬删除竞赛（同时硬删除详情和关联）
     */
    void hardDeleteCompetition(Long id);

    /**
     * 批量硬删除竞赛
     */
    void batchHardDeleteCompetitions(List<Long> ids);
}
