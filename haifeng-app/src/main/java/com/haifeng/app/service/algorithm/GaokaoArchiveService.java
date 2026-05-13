package com.haifeng.app.service.algorithm;

import com.haifeng.app.dto.algorithm.GaokaoArchiveSaveDTO;
import com.haifeng.app.vo.algorithm.*;

/**
 * 高考档案服务接口
 */
public interface GaokaoArchiveService {

    /**
     * 获取改革模式及可选科目
     *
     * @param province 省份
     * @param year     高考年份
     * @return 改革模式及可选科目
     */
    ReformModelVO getReformModel(String province, Integer year);

    /**
     * 查询位次
     *
     * @param province    省份
     * @param year        年份
     * @param subjectType 科类（物理类/历史类/理科/文科）
     * @param score       分数
     * @return 位次信息，未找到返回 null
     */
    ScoreRankVO getRank(String province, Integer year, String subjectType, Integer score);

    /**
     * 获取批次分数线列表
     *
     * @param province    省份
     * @param year        年份
     * @param subjectType 科类
     * @return 批次列表
     */
    BatchLineListVO getBatchLines(String province, Integer year, String subjectType);

    /**
     * 保存高考档案（新增或更新）
     *
     * @param dto 档案数据
     * @return 档案ID
     */
    Long saveArchive(GaokaoArchiveSaveDTO dto);

    /**
     * 获取当前用户的高考档案
     *
     * @return 档案信息，未创建返回 null
     */
    GaokaoArchiveVO getMyArchive();
}
