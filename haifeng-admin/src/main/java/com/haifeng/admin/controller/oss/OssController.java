package com.haifeng.admin.controller.oss;

import com.haifeng.admin.dto.fileload.OssConfirmUploadDTO;
import com.haifeng.admin.dto.fileload.OssPresignUploadDTO;
import com.haifeng.admin.service.oss.OssUploadService;
import com.haifeng.admin.vo.fileload.OssPresignUploadVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * OSS 直传接口
 */
@RestController
@RequestMapping("/api/v1/admin/oss")
@RequiredArgsConstructor
@RequireAdminModule("fileload_middle")  // 复用文件管理模块权限
public class OssController {

    private final OssUploadService ossUploadService;

    /**
     * 获取预签名上传 URL
     * 前端调用此接口获取上传地址，然后直接 PUT 文件到 OSS
     */
    @PostMapping("/presign-upload")
    @OperationLog(module = "文件管理", action = "获取预签名上传URL")
    public R<OssPresignUploadVO> presignUpload(@Valid @RequestBody OssPresignUploadDTO dto) {
        return R.ok(ossUploadService.presignUpload(dto));
    }

    /**
     * 确认上传完成
     * 前端直传 OSS 成功后，调用此接口保存文件元数据到数据库
     */
    @PostMapping("/confirm-upload")
    @OperationLog(module = "文件管理", action = "确认文件上传")
    public R<Long> confirmUpload(@Valid @RequestBody OssConfirmUploadDTO dto) {
        return R.ok(ossUploadService.confirmUpload(dto));
    }
}
