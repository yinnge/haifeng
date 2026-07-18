package com.haifeng.admin.controller.employment.contentManagement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeStatusDTO;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeUpdateDTO;
import com.haifeng.admin.service.employment.contentManagement.NoticeService;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 招聘内容管理 - 公告管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/content-management/notice")
@RequiredArgsConstructor
@RequireAdminModule("emp_content_notice")
@Validated
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<NoticeListVO>> list(@Valid NoticeQueryDTO dto) {
        return R.ok(noticeService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<NoticeDetailVO> detail(@PathVariable Long id) {
        return R.ok(noticeService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "招聘内容管理", action = "修改公告")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeUpdateDTO dto) {
        noticeService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "招聘内容管理", action = "删除公告")
    public R<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "招聘内容管理", action = "启用/禁用公告")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody NoticeStatusDTO dto) {
        noticeService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "招聘内容管理", action = "批量删除公告")
    public R<Void> batchDelete(@RequestBody @NotEmpty @Size(max = 100) List<Long> ids) {
        noticeService.batchDelete(ids);
        return R.ok();
    }
}
