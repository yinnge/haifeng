package com.haifeng.app.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.certificate.CertificateListQueryDTO;
import com.haifeng.app.vo.certificate.CertificateDetailVO;
import com.haifeng.app.vo.certificate.CertificateListVO;

public interface CertificateService {

    /** 任务1接口1：证书分页列表（公开，支持 category 精准过滤） */
    IPage<CertificateListVO> page(CertificateListQueryDTO dto);

    /** 任务1接口2：证书详情（登录） */
    CertificateDetailVO detail(Long certId);
}
