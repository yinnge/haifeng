package com.haifeng.app.controller.algorithm.pdf;

import com.haifeng.app.dto.algorithm.pdf.PdfProfileDTO;
import com.haifeng.app.service.algorithm.pdf.PdfProfileService;
import com.haifeng.app.vo.algorithm.pdf.PdfProfileVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * PDF 分析档案接口：读取 / 保存当前用户的考生画像与约束条件（PDF 报告用）。
 * 登录即可访问（不要求 Pro/VIP，便于用户在生成前随时完善档案）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/pdf/profile")
@RequiredArgsConstructor
@RequireLogin
public class PdfProfileController {

    private final PdfProfileService pdfProfileService;

    /**
     * 查询当前用户的 PDF 档案（未填写过则返回全空字段）
     */
    @GetMapping
    public R<PdfProfileVO> getProfile() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return R.ok(pdfProfileService.getProfile(memberId));
    }

    /**
     * 保存当前用户的 PDF 档案（存在则更新，不存在则创建）
     */
    @PostMapping
    public R<Void> saveProfile(@RequestBody PdfProfileDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        pdfProfileService.saveProfile(memberId, dto);
        return R.ok();
    }
}
