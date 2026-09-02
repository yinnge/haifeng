package com.haifeng.app.util.watermark;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * PDFBox 平铺文字水印。
 *
 * 内存注意：使用 {@code MemoryUsageSetting.setupTempFileOnly()}，让 PDFBox 把中间结构
 * 写到临时文件而不是堆内存——app 容器内存上限只有 768m，这是必须开的选项。
 */
@Slf4j
public final class PdfWatermarkUtil {

    private PdfWatermarkUtil() {
    }

    /**
     * 给 PDF 的每一页加上平铺斜向的「海枫未来规划院」水印。
     *
     * @param sourcePdf 源 PDF 字节
     * @return 已加水印的 PDF 字节
     */
    public static byte[] addWatermark(byte[] sourcePdf) {
        if (sourcePdf == null || sourcePdf.length == 0) {
            throw new IllegalArgumentException("源 PDF 内容为空");
        }
        try (InputStream in = new ByteArrayInputStream(sourcePdf);
             PDDocument doc = PDDocument.load(in, MemoryUsageSetting.setupTempFileOnly());
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDType0Font font = loadCjkFont(doc);
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(WatermarkConstant.PDF_ALPHA);
            gs.setStrokingAlphaConstant(WatermarkConstant.PDF_ALPHA);

            double rad = Math.toRadians(-30);
            for (PDPage page : doc.getPages()) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                try (PDPageContentStream cs = new PDPageContentStream(
                        doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    cs.setGraphicsStateParameters(gs);
                    for (float y = WatermarkConstant.PDF_STEP_Y * 0.5f; y < pageHeight;
                         y += WatermarkConstant.PDF_STEP_Y) {
                        for (float x = WatermarkConstant.PDF_STEP_X * 0.25f; x < pageWidth;
                             x += WatermarkConstant.PDF_STEP_X) {
                            cs.beginText();
                            cs.setFont(font, WatermarkConstant.PDF_FONT_SIZE);
                            cs.setTextMatrix(Matrix.getRotateInstance(rad, x, y));
                            cs.showText(WatermarkConstant.WATERMARK_TEXT);
                            cs.endText();
                        }
                    }
                }
            }
            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF 加水印失败", e);
            throw new IllegalStateException("PDF 加水印失败: " + e.getMessage(), e);
        }
    }

    /**
     * 加载中文字体。
     * 第三个参数 embedSubset=true 表示只嵌入实际用到的字符（7 个字），
     * 否则会把 10MB 级别的 Noto Sans SC 全量塞进每个 PDF。
     */
    private static PDType0Font loadCjkFont(PDDocument doc) throws IOException {
        try (InputStream in = PdfWatermarkUtil.class
                .getResourceAsStream(WatermarkConstant.CJK_FONT_CLASSPATH)) {
            if (in != null) {
                return PDType0Font.load(doc, in, true);
            }
        }
        throw new IllegalStateException(
                "未找到中文字体 " + WatermarkConstant.CJK_FONT_CLASSPATH + "，无法生成中文水印");
    }
}
