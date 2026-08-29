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

import java.util.List;

/**
 * 管理端 - 大学文件管理
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/fileload/college")
@RequiredArgsConstructor
@RequireAdminModule("fileload_college")
public class CollegeFileController {

    private final FileLoadService fileLoadService;

    private static final String TARGET_AUDIENCE = "college";

    /**
     * 上传大学文件
     */
    @PostMapping("/upload")
    @OperationLog(module = "大学文件管理", action = "上传文件")
    public R<Long> upload(@RequestParam("file") MultipartFile file,
                          @Valid FileLoadUploadDTO dto) {
        String operatorName = SecurityUtil.getCurrentUser() != null
                ? SecurityUtil.getCurrentUser().getUsername() : "unknown";
        return R.ok(fileLoadService.upload(file, TARGET_AUDIENCE, dto.getSubject(),
                dto.getApplicableStage(), dto.getDescription(), dto.getTag(), operatorName));
    }

    /**
     * 分页查询大学文件列表
     */
    @GetMapping("/list")
    public R<IPage<FileLoadListVO>> list(@Valid FileLoadQueryDTO dto) {
        return R.ok(fileLoadService.page(dto, TARGET_AUDIENCE));
    }

    /** 动态返回 applicable_stage 去重值（前端筛选下拉，不再写死） */
    @GetMapping("/stages")
    public R<List<String>> stages() {
        return R.ok(fileLoadService.listStages(TARGET_AUDIENCE));
    }

    /** 动态返回 subject 去重值 */
    @GetMapping("/subjects")
    public R<List<String>> subjects() {
        return R.ok(fileLoadService.listSubjects(TARGET_AUDIENCE));
    }

    /** 动态返回 tag 去重值 */
    @GetMapping("/tags")
    public R<List<String>> tags() {
        return R.ok(fileLoadService.listTags(TARGET_AUDIENCE));
    }

    /**
     * 获取大学文件详情
     */
    @GetMapping("/{id}")
    public R<FileLoadDetailVO> detail(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.detail(id));
    }

    /**
     * 修改大学文件信息
     */
    @PutMapping("/{id}")
    @OperationLog(module = "大学文件管理", action = "修改文件信息")
    public R<Void> update(@PathVariable @Min(1) Long id,
                          @Valid @RequestBody FileLoadUploadDTO dto) {
        String operatorName = SecurityUtil.getCurrentUser() != null
                ? SecurityUtil.getCurrentUser().getUsername() : "unknown";
        fileLoadService.update(id, null, dto.getSubject(), dto.getApplicableStage(),
                dto.getDescription(), dto.getTag(), dto.getVersion(), operatorName);
        return R.ok();
    }

    /**
     * 删除大学文件
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "大学文件管理", action = "删除文件")
    public R<Void> delete(@PathVariable @Min(1) Long id) {
        fileLoadService.delete(id);
        return R.ok();
    }
}
