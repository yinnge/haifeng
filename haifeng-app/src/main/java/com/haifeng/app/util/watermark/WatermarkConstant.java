package com.haifeng.app.util.watermark;

/**
 * 水印常量。
 *
 * 【重要前提】水印文本是固定内容、与用户无关，因此同一份源文件只需生成一次带水印 PDF，
 * 之后所有用户下载都复用 OSS 上的同一个缓存对象。这把「每次下载都要算一遍」的内存/CPU
 * 压力降为「每个文件算一次」，是本项目（app 容器内存上限 768m）能落地该方案的关键。
 */
public final class WatermarkConstant {

    /** 水印文本（写死，不随用户/时间变化） */
    public static final String WATERMARK_TEXT = "海枫未来规划院";

    /** 水印底图平铺单元尺寸（像素） */
    public static final int TILE_WIDTH = 420;
    public static final int TILE_HEIGHT = 300;

    /** 底图字号 */
    public static final float TILE_FONT_SIZE = 22f;

    /** 底图旋转角度（弧度，-30°） */
    public static final double TILE_ROTATE_RAD = -Math.PI / 6;

    /** 底图文字颜色（浅灰） */
    public static final int TILE_COLOR_RGB = 0x9A9A9A;

    /** 底图文字透明度 */
    public static final float TILE_ALPHA = 0.18f;

    /** PDF 内嵌水印字号（pt） */
    public static final float PDF_FONT_SIZE = 16f;

    /** PDF 内嵌水印透明度 */
    public static final float PDF_ALPHA = 0.16f;

    /** PDF 水印平铺步进（pt）：横向 / 纵向网格间距 */
    public static final float PDF_STEP_X = 250f;
    public static final float PDF_STEP_Y = 150f;

    /** 中文字体（classpath 路径，与 PDF 报告渲染共用同一份字体） */
    public static final String CJK_FONT_CLASSPATH = "/fonts/NotoSansSC-Regular.ttf";

    /** 带水印 PDF 在 OSS 上的目录前缀 */
    public static final String OSS_WATERMARK_PREFIX = "haifeng/watermarked/";

    /** 允许走水印链路的最大源文件大小（50MB）：超过则降级返回原文件，避免容器 OOM */
    public static final long MAX_WATERMARK_SOURCE_BYTES = 50L * 1024 * 1024;

    private WatermarkConstant() {
    }
}
