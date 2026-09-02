package com.haifeng.app.service.watermark;

/**
 * Office / 老格式文档 → PDF 转换器。
 *
 * 抽象成接口的原因：转换依赖 LibreOffice，而服务器只有 2 核 4G、app 容器内存上限 768m，
 * 把 LibreOffice 塞进业务容器会直接把服务拖垮。因此转换能力设计为「外部服务 + 可插拔」：
 * - 部署了 LibreOffice → 走 {@code JodDocumentToPdfConverter}，转换后加水印
 * - 没部署 → 该 Bean 不存在，调用方降级返回原文件，下载功能不受影响
 */
public interface DocumentToPdfConverter {

    /**
     * 当前转换器是否可用且支持该文件类型。
     *
     * @param fileType 文件扩展名（不含点，小写）
     */
    boolean supports(String fileType);

    /**
     * 转成 PDF。
     *
     * @param sourceBytes 源文件字节
     * @param fileType    文件扩展名（不含点，小写）
     * @return PDF 字节
     */
    byte[] convertToPdf(byte[] sourceBytes, String fileType);
}
