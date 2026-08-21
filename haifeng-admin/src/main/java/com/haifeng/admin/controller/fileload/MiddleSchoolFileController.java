package com.haifeng.admin.controller.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.fileload.FileLoadQueryDTO;
import com.haifeng.admin.dto.fileload.FileLoadUploadDTO;
import com.haifeng.admin.service.fileload.FileLoadService;
import com.haifeng.admin.vo.fileload.FileLoadDetailVO;
import com.haifeng.admin.vo.fileload.FileLoadListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import com.haifeng.common.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端 - 初中文件管理
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/fileload/middle")
@RequiredArgsConstructor
@RequireAdminModule("fileload_middle")
public class MiddleSchoolFileController {

    private final FileLoadService fileLoadService;

    private static final String TARGET_AUDIENCE = "middle_school";

    /**
     * 上传初中文件
     */
    @PostMapping("/upload")
    @OperationLog(module = "初中文件管理", action = "上传文件")
    public R<Long> upload(@RequestParam("file") MultipartFile file,
                          @Valid FileLoadUploadDTO dto) {
        String operatorName = SecurityUtil.getCurrentUser() != null
                ? SecurityUtil.getCurrentUser().getUsername() : "unknown";
        return R.ok(fileLoadService.upload(file, TARGET_AUDIENCE, dto.getSubject(),
                dto.getApplicableStage(), operatorName));
    }

    /**
     * 分页查询初中文件列表
     */
    @GetMapping("/list")
    public R<IPage<FileLoadListVO>> list(@Valid FileLoadQueryDTO dto) {
        return R.ok(fileLoadService.page(dto, TARGET_AUDIENCE));
    }

    /**
     * 获取初中文件详情
     */
    @GetMapping("/{id}")
    public R<FileLoadDetailVO> detail(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.detail(id));
    }

    /**
     * 修改初中文件信息
     */
    @PutMapping("/{id}")
    @OperationLog(module = "初中文件管理", action = "修改文件信息")
    public R<Void> update(@PathVariable @Min(1) Long id,
                          @Valid @RequestBody FileLoadUploadDTO dto) {
        String operatorName = SecurityUtil.getCurrentUser() != null
                ? SecurityUtil.getCurrentUser().getUsername() : "unknown";
        fileLoadService.update(id, null, dto.getSubject(), dto.getApplicableStage(),
                dto.getVersion(), operatorName);
        return R.ok();
    }

    /**
     * 删除初中文件
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "初中文件管理", action = "删除文件")
    public R<Void> delete(@PathVariable @Min(1) Long id) {
        fileLoadService.delete(id);
        return R.ok();
    }
}
