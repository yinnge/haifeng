package com.haifeng.admin.service.impl.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
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

        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();

        // 按软删除状态过滤（默认只查未删除的）
        if (queryDTO.getIsDeleted() != null) {
            wrapper.eq(Certificate::getIsDeleted, queryDTO.getIsDeleted());
        } else {
            wrapper.eq(Certificate::getIsDeleted, false);
        }

        // 按证书名称模糊查询
        if (StringUtils.hasText(queryDTO.getCertName())) {
            wrapper.like(Certificate::getCertName, queryDTO.getCertName());
        }
        // 按分类精确查询
        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(Certificate::getCategory, queryDTO.getCategory());
        }
        // 按等级精确查询
        if (StringUtils.hasText(queryDTO.getCertLevel())) {
            wrapper.eq(Certificate::getCertLevel, queryDTO.getCertLevel());
        }
        // 按适用专业模糊查询
        if (StringUtils.hasText(queryDTO.getApplicableMajor())) {
            wrapper.like(Certificate::getApplicableMajor, queryDTO.getApplicableMajor());
        }
        wrapper.orderByDesc(Certificate::getCreatedAt);

        IPage<Certificate> result = certificateMapper.selectPage(page, wrapper);

        return result.convert(this::convertToListVO);
    }

    @Override
    public CertificateDetailVO getCertificateDetail(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null || certificate.getIsDeleted()) {
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
        Certificate existing = certificateMapper.selectById(updateDTO.getId());
        if (existing == null || existing.getIsDeleted()) {
            throw new BusinessException(404, "证书不存在");
        }

        // 如果修改了名称，检查是否重复
        if (StringUtils.hasText(updateDTO.getCertName())
                && !updateDTO.getCertName().equals(existing.getCertName())) {
            if (certificateMapper.existsByCertName(updateDTO.getCertName())) {
                throw new BusinessException(400, "证书名称已存在");
            }
        }

        BeanUtils.copyProperties(updateDTO, existing);
        certificateMapper.updateById(existing);
        log.info("更新证书成功，id={}", updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteCertificate(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null || certificate.getIsDeleted()) {
            throw new BusinessException(404, "证书不存在");
        }

        certificate.setIsDeleted(true);
        certificateMapper.updateById(certificate);
        log.info("软删除证书成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteCertificate(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
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
        certificateMapper.deleteBatchIds(ids);
        log.info("批量硬删除证书成功，ids={}", ids);
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
