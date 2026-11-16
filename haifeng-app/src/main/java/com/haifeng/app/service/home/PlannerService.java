package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.vo.home.PlannerAppointmentVO;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;

public interface PlannerService {

    /** 分页查询指定服务对象（高考/考研）的规划师，按 sort_order ASC, id DESC 排序 */
    IPage<PlannerListVO> page(PlannerQueryDTO dto);

    /** 查询规划师详情（未软删除，不存在抛 404） */
    PlannerDetailVO detail(Long id);

    /** 查询规划师预约信息（未软删除，不存在抛 404） */
    PlannerAppointmentVO appointment(Long id);
}
