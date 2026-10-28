package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityChannelQueryDTO;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.vo.university.ChannelOptionVO;
import com.haifeng.app.vo.university.UniversityBriefVO;
import com.haifeng.app.vo.university.UniversityChannelListVO;
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.app.vo.university.UniversityStatsVO;

import java.util.List;

public interface UniversityService {

    /**
     * 分页查询院校列表（仅 status=1）；多筛选 AND；name LIKE；
     * tags 走数组包含匹配（AND 交集）；channelCode 走特殊招生通道关联表（统招不传参）；
     * 排序仅 sort_order DESC
     */
    IPage<UniversityListVO> page(UniversityQueryDTO dto);

    /**
     * 院校统计概览（985/211/双一流/本科数量）：仅 status=1，不受列表筛选影响，免登录
     */
    UniversityStatsVO stats();

    /**
     * 招生类型筛选下拉选项：查 t_special_channel（is_active=true，按 sort_order 升序），免登录
     */
    List<ChannelOptionVO> listSpecialChannelOptions();

    /**
     * 院校详情：联表查询 t_universities + t_universities_detail
     * 任一不存在或 status != 1 → BusinessException(NOT_FOUND)
     */
    UniversityDetailVO detail(Long universityId);

    /**
     * 分页查询院校的特殊招生渠道
     */
    IPage<UniversityChannelListVO> pageChannels(Long universityId, UniversityChannelQueryDTO dto);

    /**
     * 查询所有可用的招生渠道选项（去重）
     */
    List<ChannelOptionVO> listChannelOptions();

    /**
     * 根据名称查询院校简要信息
     */
    UniversityBriefVO getByName(String name);
}
