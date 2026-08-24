package com.haifeng.app.service.algorithm.pdf;

import com.haifeng.app.dto.algorithm.pdf.PdfProfileDTO;
import com.haifeng.app.vo.algorithm.pdf.PdfProfileVO;

/**
 * PDF 分析档案服务：读取 / 保存当前用户的考生画像与约束条件（PDF 报告用）。
 */
public interface PdfProfileService {

    /**
     * 查询当前用户的 PDF 档案（档案不存在时返回全空对象，不抛错）。
     */
    PdfProfileVO getProfile(Long memberId);

    /**
     * 保存当前用户的 PDF 档案（存在则更新，不存在则创建新档案行）。
     */
    void saveProfile(Long memberId, PdfProfileDTO dto);
}
