package com.haifeng.admin.vo.common;

import lombok.Data;

/**
 * 管理端 - 图片上传结果
 */
@Data
public class ImageUploadVO {

    /** 可直接访问的完整 URL（公共读 bucket） */
    private String url;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long size;

    public ImageUploadVO() {
    }

    public ImageUploadVO(String url, String fileName, Long size) {
        this.url = url;
        this.fileName = fileName;
        this.size = size;
    }
}
