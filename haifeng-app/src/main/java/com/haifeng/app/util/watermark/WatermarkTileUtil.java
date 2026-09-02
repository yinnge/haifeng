package com.haifeng.app.util.watermark;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * 生成「可平铺」的水印底图（PNG，透明背景 + 半透明斜向文字）。
 *
 * 用途有两个：
 * 1. Excel 的 sheet 背景图（POI setBackgroundPicture，普通视图即可见）
 * 2. PDF 报告的 @page 背景（Thymeleaf + openhtmltopdf，每页自动平铺）
 *
 * 生成结果缓存在内存：水印文本固定，只需渲染一次。
 */
@Slf4j
public final class WatermarkTileUtil {

    static {
        // Docker（alpine JRE）无 X11，显式声明 headless，避免 AWT 初始化时抛 HeadlessException
        System.setProperty("java.awt.headless", "true");
    }

    private static volatile byte[] cachedTile;

    private WatermarkTileUtil() {
    }

    /** 水印底图 PNG 字节（带缓存） */
    public static byte[] buildTilePng() {
        byte[] cached = cachedTile;
        if (cached != null) {
            return cached;
        }
        synchronized (WatermarkTileUtil.class) {
            if (cachedTile == null) {
                cachedTile = render();
            }
            return cachedTile;
        }
    }

    /** 水印底图的 data URI，可直接塞进 CSS 的 url(...) */
    public static String buildTileDataUri() {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(buildTilePng());
    }

    private static byte[] render() {
        BufferedImage image = new BufferedImage(
                WatermarkConstant.TILE_WIDTH, WatermarkConstant.TILE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WatermarkConstant.TILE_ALPHA));
            g.setColor(new Color(WatermarkConstant.TILE_COLOR_RGB));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(loadCjkFont().deriveFont(Font.PLAIN, WatermarkConstant.TILE_FONT_SIZE));

            // 以平铺单元中心为轴旋转，保证平铺后纹理连续
            g.rotate(WatermarkConstant.TILE_ROTATE_RAD,
                    WatermarkConstant.TILE_WIDTH / 2.0, WatermarkConstant.TILE_HEIGHT / 2.0);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(WatermarkConstant.WATERMARK_TEXT);
            int x = (WatermarkConstant.TILE_WIDTH - textWidth) / 2;
            int baseline = WatermarkConstant.TILE_HEIGHT / 2
                    + (fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(WatermarkConstant.WATERMARK_TEXT, x, baseline);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new IllegalStateException("生成水印底图失败", e);
        }
        return out.toByteArray();
    }

    /**
     * 加载中文字体：优先 classpath 内置字体（与 PDF 报告共用 NotoSansSC-Regular.ttf），
     * 缺失时回退到系统字体（Docker 内可能没有中文字体，那时中文会渲染成方框）。
     */
    public static Font loadCjkFont() {
        try (InputStream in = WatermarkTileUtil.class
                .getResourceAsStream(WatermarkConstant.CJK_FONT_CLASSPATH)) {
            if (in != null) {
                return Font.createFont(Font.TRUETYPE_FONT, in);
            }
        } catch (Exception e) {
            log.warn("加载 classpath 中文字体失败，回退系统字体: {}", e.getMessage());
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, (int) WatermarkConstant.TILE_FONT_SIZE);
    }
}
