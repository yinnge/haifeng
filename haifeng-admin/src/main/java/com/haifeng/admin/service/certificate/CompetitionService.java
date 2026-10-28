package com.haifeng.admin.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;
import com.haifeng.admin.vo.major.ImportResultVO;

import org.springframework.web.multipart.MultipartFile;

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
     * 启用竞赛（恢复已禁用的竞赛）
     */
    void enableCompetition(Long id);

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

    /**
     * 批量导入竞赛主表（读取 sheet「竞赛主表」）。
     * 竞赛名称唯一：库无则新增；库有（启用）则补空不覆盖；库有（软删除）则冲突报错。
     */
    ImportResultVO importMain(MultipartFile file);

    /**
     * 批量导入竞赛详情（读取 竞赛次表 / 基本信息 / 竞赛规则 / 参赛流程指南 / 奖项设置展示 共 5 个 sheet）。
     * 所有行的竞赛名称必须已存在于主表，否则整批回滚并报告「第[Sheet]第X行: 竞赛名称[xxx]不存在」。
     * 补空不覆盖；JSONB sheet 的每一行对应一条 {title, content} 元素。
     */
    ImportResultVO importDetail(MultipartFile file);
}
