package com.haifeng.admin.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.CompetitionMajorAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionMajorQueryDTO;
import com.haifeng.admin.vo.certificate.CompetitionMajorVO;

import java.util.List;

public interface CompetitionMajorService {

    /**
     * 分页查询竞赛-专业关联列表
     */
    IPage<CompetitionMajorVO> listCompetitionMajors(CompetitionMajorQueryDTO queryDTO);

    /**
     * 根据竞赛ID查询关联的专业列表
     */
    List<CompetitionMajorVO> listByCompetitionId(Long competitionId);

    /**
     * 根据专业ID查询关联的竞赛列表
     */
    List<CompetitionMajorVO> listByMajorId(Long majorId);

    /**
     * 新增竞赛-专业关联（基于名称查找ID）
     */
    Long addCompetitionMajor(CompetitionMajorAddDTO addDTO);

    /**
     * 删除关联（硬删除）
     */
    void deleteCompetitionMajor(Long id);

    /**
     * 批量删除关联（硬删除）
     */
    void batchDeleteCompetitionMajors(List<Long> ids);
}
