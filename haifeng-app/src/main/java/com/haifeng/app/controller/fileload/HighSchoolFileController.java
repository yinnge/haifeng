package com.haifeng.app.controller.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.service.fileload.FileLoadService;
import com.haifeng.app.vo.fileload.FileLoadDetailVO;
import com.haifeng.app.vo.fileload.FileLoadListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C 端高中文件
 * 列表：公开（免登录）
 * 详情/预览/下载：仅VIP用户
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/fileload/high")
@RequiredArgsConstructor
public class HighSchoolFileController {

    private final FileLoadService fileLoadService;

    private static final String TARGET_AUDIENCE = "high_school";

    /**
     * 分页查询高中文件列表（公开，免登录）
     */
    @GetMapping("/list")
    public R<IPage<FileLoadListVO>> list(@Valid BasePageQueryDTO dto,
                                         @RequestParam(required = false) String subject,
                                         @RequestParam(required = false) String applicableStage,
                                         @RequestParam(required = false) String tag) {
        return R.ok(fileLoadService.page(dto, TARGET_AUDIENCE, subject, applicableStage, tag));
    }

    /** 动态返回 applicable_stage 去重值（前端按钮筛选） */
    @GetMapping("/stages")
    public R<List<String>> stages() {
        return R.ok(fileLoadService.listStages(TARGET_AUDIENCE));
    }

    /** 动态返回 subject 去重值（前端下拉筛选） */
    @GetMapping("/subjects")
    public R<List<String>> subjects() {
        return R.ok(fileLoadService.listSubjects(TARGET_AUDIENCE));
    }

    /** 动态返回 tag 去重值（前端下拉筛选） */
    @GetMapping("/tags")
    public R<List<String>> tags() {
        return R.ok(fileLoadService.listTags(TARGET_AUDIENCE));
    }

    /**
     * 获取高中文件详情（仅VIP）
     */
    @RequireLogin
    @GetMapping("/{id}")
    public R<FileLoadDetailVO> detail(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.detail(id));
    }

    /**
     * 获取高中文件预览URL（仅VIP）
     */
    @RequireLogin
    @GetMapping("/{id}/preview")
    public R<String> preview(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.getPreviewUrl(id));
    }

    /**
     * 获取高中文件下载URL（仅VIP + 悲观锁防误触）
     */
    @RequireVip
    @GetMapping("/{id}/download")
    public R<String> download(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.getDownloadUrl(id));
    }
}
