package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityChannelQueryDTO;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.vo.university.ChannelOptionVO;
import com.haifeng.app.vo.university.UniversityBriefVO;
import com.haifeng.app.vo.university.UniversityChannelListVO;
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.app.vo.university.UniversityListVO;

import java.util.List;

public interface UniversityService {

    /**
     * 分页查询院校列表（仅 status=1）；多筛选 AND；name LIKE；排序 sort_order ASC, id DESC
     */
    IPage<UniversityListVO> page(UniversityQueryDTO dto);

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
