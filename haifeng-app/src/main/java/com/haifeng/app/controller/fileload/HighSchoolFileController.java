package com.haifeng.app.controller.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.service.fileload.FileLoadService;
import com.haifeng.app.vo.fileload.FileLoadDetailVO;
import com.haifeng.app.vo.fileload.FileLoadListVO;
import com.haifeng.app.vo.fileload.NetdiskInfoVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequireTeacher;
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
     * 老师专属文件受众（面向人群固定为 teacher，与 high_school 互斥）
     */
    private static final String TEACHER_AUDIENCE = "teacher";

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

    /**
     * 获取百度网盘资源取码信息（仅VIP：返回分享链接 + 提取码）
     */
    @RequireVip
    @GetMapping("/{id}/netdisk")
    public R<NetdiskInfoVO> netdisk(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.getNetdiskInfo(id));
    }

    // ===================== 老师专属文件（面向人群固定为 teacher，仅老师身份可访问） =====================

    /**
     * 分页查询老师专属文件列表（仅老师身份）
     */

    @GetMapping("/teacher/list")
    public R<IPage<FileLoadListVO>> teacherList(@Valid BasePageQueryDTO dto,
                                               @RequestParam(required = false) String subject,
                                               @RequestParam(required = false) String applicableStage,
                                               @RequestParam(required = false) String tag) {
        return R.ok(fileLoadService.page(dto, TEACHER_AUDIENCE, subject, applicableStage, tag));
    }

    /** 老师专属：动态返回 applicable_stage 去重值 */

    @GetMapping("/teacher/stages")
    public R<List<String>> teacherStages() {
        return R.ok(fileLoadService.listStages(TEACHER_AUDIENCE));
    }

    /** 动态返回 subject 去重值 */

    @GetMapping("/teacher/subjects")
    public R<List<String>> teacherSubjects() {
        return R.ok(fileLoadService.listSubjects(TEACHER_AUDIENCE));
    }

    /** 动态返回 tag 去重值 */

    @GetMapping("/teacher/tags")
    public R<List<String>> teacherTags() {
        return R.ok(fileLoadService.listTags(TEACHER_AUDIENCE));
    }

    /**
     * 获取老师专属文件详情（仅老师身份）
     */
    @RequireTeacher
    @GetMapping("/teacher/{id}")
    public R<FileLoadDetailVO> teacherDetail(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.detail(id));
    }

    /**
     * 获取老师专属文件预览URL（仅老师身份）
     */
    @RequireTeacher
    @GetMapping("/teacher/{id}/preview")
    public R<String> teacherPreview(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.getPreviewUrl(id));
    }

    /**
     * 获取老师专属文件下载URL（仅老师身份）
     */
    @RequireTeacher
    @GetMapping("/teacher/{id}/download")
    public R<String> teacherDownload(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.getDownloadUrl(id));
    }

    /**
     * 获取老师专属百度网盘资源取码信息（仅老师身份）
     */
    @RequireTeacher
    @GetMapping("/teacher/{id}/netdisk")
    public R<NetdiskInfoVO> teacherNetdisk(@PathVariable @Min(1) Long id) {
        return R.ok(fileLoadService.getNetdiskInfo(id));
    }
}
