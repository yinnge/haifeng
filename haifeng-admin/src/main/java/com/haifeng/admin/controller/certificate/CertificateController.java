package com.haifeng.admin.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.BatchDeleteDTO;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
import com.haifeng.admin.dto.certificate.CertificateUpdateDTO;
import com.haifeng.admin.service.certificate.CertificateService;
import com.haifeng.admin.vo.certificate.CertificateDetailVO;
import com.haifeng.admin.vo.certificate.CertificateListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/list")
    public R<IPage<CertificateListVO>> list(@Valid CertificateQueryDTO queryDTO) {
        return R.ok(certificateService.listCertificates(queryDTO));
    }

    @GetMapping("/{id}")
    public R<CertificateDetailVO> detail(@PathVariable Long id) {
        return R.ok(certificateService.getCertificateDetail(id));
    }

    @PostMapping("/add")
    @OperationLog(module = "竞赛证书管理", action = "新增证书")
    public R<Long> add(@Valid @RequestBody CertificateAddDTO addDTO) {
        return R.ok(certificateService.addCertificate(addDTO));
    }

    @PutMapping("/update")
    @OperationLog(module = "竞赛证书管理", action = "更新证书")
    public R<Void> update(@Valid @RequestBody CertificateUpdateDTO updateDTO) {
        certificateService.updateCertificate(updateDTO);
        return R.ok();
    }

    @DeleteMapping("/soft/{id}")
    @OperationLog(module = "竞赛证书管理", action = "软删除证书")
    public R<Void> softDelete(@PathVariable Long id) {
        certificateService.softDeleteCertificate(id);
        return R.ok();
    }

    @DeleteMapping("/hard/{id}")
    @OperationLog(module = "竞赛证书管理", action = "硬删除证书")
    public R<Void> hardDelete(@PathVariable Long id) {
        certificateService.hardDeleteCertificate(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "竞赛证书管理", action = "批量硬删除证书")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO batchDTO) {
        certificateService.batchHardDeleteCertificates(batchDTO.getIds());
        return R.ok();
    }
}
