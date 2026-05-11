package com.haifeng.admin.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
import com.haifeng.admin.dto.certificate.CertificateUpdateDTO;
import com.haifeng.admin.vo.certificate.CertificateDetailVO;
import com.haifeng.admin.vo.certificate.CertificateListVO;

import java.util.List;

public interface CertificateService {

    /**
     * 分页查询证书列表
     */
    IPage<CertificateListVO> listCertificates(CertificateQueryDTO queryDTO);

    /**
     * 获取证书详情
     */
    CertificateDetailVO getCertificateDetail(Long id);

    /**
     * 新增证书
     */
    Long addCertificate(CertificateAddDTO addDTO);

    /**
     * 更新证书
     */
    void updateCertificate(CertificateUpdateDTO updateDTO);

    /**
     * 软删除证书
     */
    void softDeleteCertificate(Long id);

    /**
     * 硬删除证书
     */
    void hardDeleteCertificate(Long id);

    /**
     * 批量硬删除证书
     */
    void batchHardDeleteCertificates(List<Long> ids);
}
