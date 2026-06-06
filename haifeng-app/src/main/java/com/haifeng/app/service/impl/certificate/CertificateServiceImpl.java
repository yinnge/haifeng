package com.haifeng.app.service.impl.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.certificate.CertificateListQueryDTO;
import com.haifeng.app.service.certificate.CertificateService;
import com.haifeng.app.vo.certificate.CertificateDetailVO;
import com.haifeng.app.vo.certificate.CertificateListVO;
import com.haifeng.common.entity.certificate.Certificate;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CertificateMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateMapper certificateMapper;

    @Override
    public IPage<CertificateListVO> page(CertificateListQueryDTO dto) {
        Page<Certificate> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getIsDeleted, false)
                .eq(StringUtils.hasText(dto.getCategory()),
                        Certificate::getCategory, dto.getCategory())
                .like(StringUtils.hasText(dto.getCertName()),
                        Certificate::getCertName, dto.getCertName())
                .orderByAsc(Certificate::getId);

        IPage<Certificate> entityPage = certificateMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public CertificateDetailVO detail(Long certId) {
        Certificate cert = certificateMapper.selectOne(
                new LambdaQueryWrapper<Certificate>()
                        .eq(Certificate::getId, certId)
                        .eq(Certificate::getIsDeleted, false));
        if (cert == null) {
            log.debug("证书不存在或已删除, certId={}", certId);
            throw new BusinessException(ResultCode.NOT_FOUND, "证书不存在");
        }

        return CertificateDetailVO.builder()
                .id(cert.getId())
                .certName(cert.getCertName())
                .category(cert.getCategory())
                .certLevel(cert.getCertLevel())
                .applicableMajor(cert.getApplicableMajor())
                .registrationTime(cert.getRegistrationTime())
                .examTime(cert.getExamTime())
                .examFee(cert.getExamFee())
                .certIntro(cert.getCertIntro())
                .examRequirements(cert.getExamRequirements())
                .examArrangement(cert.getExamArrangement())
                .officialWebsite(cert.getOfficialWebsite())
                .build();
    }

    private CertificateListVO toListVO(Certificate e) {
        return CertificateListVO.builder()
                .id(e.getId())
                .certName(e.getCertName())
                .category(e.getCategory())
                .certLevel(e.getCertLevel())
                .applicableMajor(e.getApplicableMajor())
                .registrationTime(e.getRegistrationTime())
                .examTime(e.getExamTime())
                .examFee(e.getExamFee())
                .certIntro(e.getCertIntro())
                .build();
    }
}
