package com.haifeng.app.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.certificate.CertificateListQueryDTO;
import com.haifeng.app.service.certificate.CertificateService;
import com.haifeng.app.vo.certificate.CertificateDetailVO;
import com.haifeng.app.vo.certificate.CertificateListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端证书管理（spec 任务1）
 * 接口1 公开，接口2 需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /** 任务1接口1：证书列表（公开，支持 category 精准过滤） */
    @GetMapping("/list")
    public R<IPage<CertificateListVO>> list(@Valid CertificateListQueryDTO dto) {
        return R.ok(certificateService.page(dto));
    }

    /** 任务1接口2：证书详情（登录） */
    @RequireLogin
    @GetMapping("/{certId}/detail")
    public R<CertificateDetailVO> detail(@PathVariable Long certId) {
        return R.ok(certificateService.detail(certId));
    }
}
