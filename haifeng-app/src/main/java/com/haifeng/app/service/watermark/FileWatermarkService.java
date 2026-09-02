package com.haifeng.app.service.watermark;

import com.haifeng.common.entity.resource.FileInfo;

/**
 * 文件下载水印服务。
 *
 * 策略：下载统一输出「带水印 PDF」。
 * - 源文件本身就是 PDF → 直接加水印
 * - 图片 → 先包成单页 PDF，再加水印
 * - Office（word/ppt/excel 及老格式）→ 先经 LibreOffice 转 PDF，再加水印
 * - 其余类型（zip/cad/txt 预览型除外）→ 原样返回，不加水印
 *
 * 缓存：水印文本固定，所以一个源文件只生成一次，结果存在 OSS 上供所有用户复用。
 * 降级：转换服务不可用 / 文件过大 / 生成失败 → 返回原文件签名 URL，下载功能不中断。
 */
public interface FileWatermarkService {

    /**
     * 获取带水印文件的下载地址。
     *
     * @param fileInfo 文件记录
     * @return 预签名下载 URL（带源文件名 disposition）
     */
    String getWatermarkedDownloadUrl(FileInfo fileInfo);
}
