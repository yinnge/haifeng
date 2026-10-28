package com.haifeng.admin.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateBatchStatusDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
import com.haifeng.admin.dto.certificate.CertificateStatusDTO;
import com.haifeng.admin.dto.certificate.CertificateUpdateDTO;
import com.haifeng.admin.vo.certificate.CertificateDetailVO;
import com.haifeng.admin.vo.certificate.CertificateListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import org.springframework.web.multipart.MultipartFile;

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
     * 修改证书状态（启用/禁用）
     */
    void updateCertificateStatus(Long id, CertificateStatusDTO dto);

    /**
     * 软删除证书
     */
    void softDeleteCertificate(Long id);

    /**
     * 硬删除证书
     */
    void hardDeleteCertificate(Long id);

    /**
     * 批量硬删除证书（物理删除）
     */
    void batchHardDeleteCertificates(List<Long> ids);

    /**
     * 批量修改证书状态（启用/禁用）
     */
    void batchUpdateCertificateStatus(CertificateBatchStatusDTO dto);

    /**
     * 批量导入证书（EasyExcel）。
     * 规则：cert_name 已存在(启用)→补空不覆盖；已存在(软删除)→冲突报错；表内重复 cert_name→报错；库无→新增。
     * 校验不过或任意行失败均整批回滚。
     */
    ImportResultVO importData(MultipartFile file);
}
