package com.haifeng.app.service.consultation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.consultation.ConsultationAddDTO;
import com.haifeng.app.dto.consultation.ConsultationQueryDTO;
import com.haifeng.app.vo.consultation.ConsultationDetailVO;
import com.haifeng.app.vo.consultation.ConsultationListVO;
import com.haifeng.app.vo.consultation.ConsultationUnhandledCountVO;
import com.haifeng.common.enums.ConsultationBizType;

public interface ConsultationService {

    /**
     * C 端提交咨询留言：校验业务对象与接收教师，写入留言并给教师发通知。
     */
    Long submit(ConsultationBizType bizType, Long bizId, ConsultationAddDTO dto);

    /**
     * 教师端分页查询「我收到的留言」。
     */
    IPage<ConsultationListVO> page(ConsultationQueryDTO dto);

    /**
     * 教师端留言详情，非本人留言返回 404。
     */
    ConsultationDetailVO detail(Long id);

    /**
     * 标记已处理，重复调用幂等。
     */
    void markHandled(Long id);

    /**
     * 教师端未处理留言数。
     */
    ConsultationUnhandledCountVO unhandledCount();
}
