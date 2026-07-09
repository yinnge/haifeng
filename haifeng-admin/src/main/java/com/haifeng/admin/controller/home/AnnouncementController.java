package com.haifeng.admin.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.*;
import com.haifeng.admin.service.home.AnnouncementService;
import com.haifeng.admin.vo.home.*;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 首页管理 - 公告管理
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/home/announcement")
@RequiredArgsConstructor
@RequireAdminModule("home_announcement")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 分页查询公告列表
     */
    @GetMapping("/list")
    public R<IPage<AnnouncementListVO>> list(@Valid AnnouncementQueryDTO dto) {
        return R.ok(announcementService.page(dto));
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public R<AnnouncementDetailVO> detail(@PathVariable Long id) {
        return R.ok(announcementService.detail(id));
    }

    /**
     * 新增公告
     */
    @PostMapping
    @OperationLog(module = "首页管理", action = "新增公告")
    public R<Long> add(@Valid @RequestBody AnnouncementAddDTO dto) {
        return R.ok(announcementService.add(dto));
    }

    /**
     * 修改公告
     */
    @PutMapping("/{id}")
    @OperationLog(module = "首页管理", action = "修改公告")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AnnouncementUpdateDTO dto) {
        announcementService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改公告状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "首页管理", action = "修改公告状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        announcementService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 硬删除公告
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "首页管理", action = "硬删除公告")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return R.ok();
    }
}
