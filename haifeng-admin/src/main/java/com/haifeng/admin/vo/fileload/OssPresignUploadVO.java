package com.haifeng.admin.vo.fileload;

import lombok.Builder;
import lombok.Data;

/**
 * OSS 预签名上传响应 VO
 */
@Data
@Builder
public class OssPresignUploadVO {

    /** 预签名上传 URL（前端直接 PUT 到此 URL） */
    private String uploadUrl;

    /** OSS 对象 key（上传成功后需回传给后端确认） */
    private String objectKey;

    /** 上传请求头（需要带上这些 header） */
    private String contentType;
}
