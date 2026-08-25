package com.haifeng.admin.service.oss;

import com.haifeng.admin.dto.fileload.OssConfirmUploadDTO;
import com.haifeng.admin.dto.fileload.OssPresignUploadDTO;
import com.haifeng.admin.vo.fileload.OssPresignUploadVO;

/**
 * OSS 直传服务接口
 */
public interface OssUploadService {

    /**
     * 生成预签名上传 URL
     * @param dto 包含文件名和元数据
     * @return 预签名上传信息
     */
    OssPresignUploadVO presignUpload(OssPresignUploadDTO dto);

    /**
     * 确认上传并保存文件元数据
     * @param dto 包含 objectKey 和文件元数据
     * @return 文件ID
     */
    Long confirmUpload(OssConfirmUploadDTO dto);
}
