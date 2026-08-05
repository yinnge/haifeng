package com.haifeng.admin.service.impl.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateBatchStatusDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
import com.haifeng.admin.dto.certificate.CertificateStatusDTO;
import com.haifeng.admin.dto.certificate.CertificateUpdateDTO;
import com.haifeng.admin.service.certificate.CertificateService;
import com.haifeng.admin.vo.certificate.CertificateDetailVO;
import com.haifeng.admin.vo.certificate.CertificateListVO;
import com.haifeng.common.entity.certificate.Certificate;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CertificateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateMapper certificateMapper;

    @Override
    public IPage<CertificateListVO> listCertificates(CertificateQueryDTO queryDTO) {
        Page<Certificate> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置
        IPage<Certificate> result = certificateMapper.selectPageIgnoreLogicDelete(
                page,
                queryDTO.getIsDeleted(),
                queryDTO.getCertName(),
                queryDTO.getCategory(),
                queryDTO.getCertLevel(),
                queryDTO.getApplicableMajor()
        );

        return result.convert(this::convertToListVO);
    }

    @Override
    public CertificateDetailVO getCertificateDetail(Long id) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置，可以查询已禁用的数据
        Certificate certificate = certificateMapper.findByIdIgnoreLogicDelete(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }
        return convertToDetailVO(certificate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCertificate(CertificateAddDTO addDTO) {
        // 检查证书名称是否重复
        if (certificateMapper.existsByCertName(addDTO.getCertName())) {
            throw new BusinessException(400, "证书名称已存在");
        }

        Certificate certificate = new Certificate();
        BeanUtils.copyProperties(addDTO, certificate);
        certificate.setIsDeleted(false);

        certificateMapper.insert(certificate);
        log.info("新增证书成功，id={}, certName={}", certificate.getId(), certificate.getCertName());
        return certificate.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCertificate(CertificateUpdateDTO updateDTO) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置，可以更新已禁用的数据
        Certificate existing = certificateMapper.findByIdIgnoreLogicDelete(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "证书不存在");
        }

        // 如果修改了名称，检查是否重复
        if (StringUtils.hasText(updateDTO.getCertName())
                && !updateDTO.getCertName().equals(existing.getCertName())) {
            if (certificateMapper.existsByCertName(updateDTO.getCertName())) {
                throw new BusinessException(400, "证书名称已存在");
            }
        }

        // 使用自定义SQL更新，绕过MyBatis-Plus全局逻辑删除配置
        certificateMapper.updateByIdIgnoreLogicDelete(
                updateDTO.getId(),
                updateDTO.getCertName(),
                updateDTO.getCategory(),
                updateDTO.getCertLevel(),
                updateDTO.getApplicableMajor(),
                updateDTO.getRegistrationTime(),
                updateDTO.getExamTime(),
                updateDTO.getExamFee(),
                updateDTO.getCertIntro(),
                updateDTO.getExamRequirements(),
                updateDTO.getExamArrangement(),
                updateDTO.getOfficialWebsite()
        );
        log.info("更新证书成功，id={}", updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCertificateStatus(Long id, CertificateStatusDTO dto) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置
        Certificate certificate = certificateMapper.findByIdIgnoreLogicDelete(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        // 使用自定义SQL直接更新is_deleted字段
        certificateMapper.updateIsDeletedById(id, dto.getIsDeleted());
        log.info("修改证书状态成功，id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteCertificate(Long id) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置
        Certificate certificate = certificateMapper.findByIdIgnoreLogicDelete(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        // 使用自定义SQL直接更新is_deleted字段
        certificateMapper.updateIsDeletedById(id, true);
        log.info("软删除证书成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteCertificate(Long id) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置，可以删除已禁用的数据
        Certificate certificate = certificateMapper.findByIdIgnoreLogicDelete(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        certificateMapper.deleteById(id);
        log.info("硬删除证书成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDeleteCertificates(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 使用自定义SQL进行物理删除，绕过MyBatis-Plus全局逻辑删除配置
        certificateMapper.physicalDeleteBatchByIds(ids);
        log.info("批量硬删除证书成功，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCertificateStatus(CertificateBatchStatusDTO dto) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return;
        }
        // 使用自定义SQL批量更新is_deleted字段，绕过MyBatis-Plus全局逻辑删除配置
        certificateMapper.batchUpdateIsDeletedByIds(dto.getIds(), dto.getIsDeleted());
        log.info("批量修改证书状态成功，ids={}, isDeleted={}", dto.getIds(), dto.getIsDeleted());
    }

    private CertificateListVO convertToListVO(Certificate certificate) {
        return CertificateListVO.builder()
                .id(certificate.getId())
                .certName(certificate.getCertName())
                .category(certificate.getCategory())
                .certLevel(certificate.getCertLevel())
                .applicableMajor(certificate.getApplicableMajor())
                .registrationTime(certificate.getRegistrationTime())
                .examTime(certificate.getExamTime())
                .examFee(certificate.getExamFee())
                .isDeleted(certificate.getIsDeleted())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .build();
    }

    private CertificateDetailVO convertToDetailVO(Certificate certificate) {
        return CertificateDetailVO.builder()
                .id(certificate.getId())
                .certName(certificate.getCertName())
                .category(certificate.getCategory())
                .certLevel(certificate.getCertLevel())
                .applicableMajor(certificate.getApplicableMajor())
                .registrationTime(certificate.getRegistrationTime())
                .examTime(certificate.getExamTime())
                .examFee(certificate.getExamFee())
                .certIntro(certificate.getCertIntro())
                .examRequirements(certificate.getExamRequirements())
                .examArrangement(certificate.getExamArrangement())
                .officialWebsite(certificate.getOfficialWebsite())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .build();
    }
}
