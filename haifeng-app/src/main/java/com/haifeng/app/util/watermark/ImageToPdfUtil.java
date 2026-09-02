package com.haifeng.app.util.watermark;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 图片 → PDF：把图片包成一页 PDF，再统一走 {@link PdfWatermarkUtil} 加水印。
 * 这样全站只需要一套水印代码（都针对 PDF），不必为图片单独实现一套绘制逻辑。
 */
@Slf4j
public final class ImageToPdfUtil {

    /** 页面四周留白（pt） */
    private static final float MARGIN = 24f;

    /** 图片最长边上限（pt）：PDF 规范单页边长上限为 200 英寸 = 14400pt，这里留足余量 */
    private static final float MAX_SIDE_PT = 1400f;

    private ImageToPdfUtil() {
    }

    /**
     * 把图片转换为单页 PDF（页面尺寸自适应图片，超长边等比缩放）。
     *
     * @param imageBytes 图片原始字节（png / jpg / jpeg / bmp / gif）
     * @return PDF 字节
     */
    public static byte[] imageToPdf(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("图片内容为空");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IllegalArgumentException("无法解析图片内容（格式不支持或文件已损坏）");
            }
            float drawWidth = image.getWidth();
            float drawHeight = image.getHeight();
            // 等比缩放：限制最长边，避免超出 PDF 单页尺寸上限
            float maxSide = Math.max(drawWidth, drawHeight);
            if (maxSide > MAX_SIDE_PT) {
                float scale = MAX_SIDE_PT / maxSide;
                drawWidth *= scale;
                drawHeight *= scale;
            }

            try (PDDocument doc = new PDDocument();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                PDPage page = new PDPage(new PDRectangle(drawWidth + MARGIN * 2, drawHeight + MARGIN * 2));
                doc.addPage(page);
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageBytes, "source-image");
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.drawImage(pdImage, MARGIN, MARGIN, drawWidth, drawHeight);
                }
                doc.save(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            log.error("图片转 PDF 失败", e);
            throw new IllegalStateException("图片转 PDF 失败: " + e.getMessage(), e);
        }
    }
}
