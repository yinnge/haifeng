package com.haifeng.admin.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.system.AdminLogBatchDeleteDTO;
import com.haifeng.admin.dto.system.AdminLogQueryDTO;
import com.haifeng.admin.service.system.AdminLogService;
import com.haifeng.admin.vo.system.AdminLogDetailVO;
import com.haifeng.admin.vo.system.AdminLogListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统管理 - 操作日志查询与批量清理（按ID / 一个月前 / 全部）
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/system/logs")
@RequiredArgsConstructor
@RequireAdminModule("system_log")
public class AdminLogController {

    private final AdminLogService adminLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/list")
    public R<IPage<AdminLogListVO>> list(@Valid AdminLogQueryDTO dto) {
        return R.ok(adminLogService.page(dto));
    }

    /**
     * 获取操作日志详情
     */
    @GetMapping("/{id}")
    public R<AdminLogDetailVO> detail(@PathVariable Long id) {
        return R.ok(adminLogService.detail(id));
    }

    /**
     * 批量删除操作日志
     * type: ids-按ID批量删除 / lastMonth-删除一个月前的日志 / all-全部删除
     */
    @PostMapping("/batch")
    @OperationLog(module = "系统管理", action = "批量删除操作日志")
    public R<Integer> batchDelete(@Valid @RequestBody AdminLogBatchDeleteDTO dto) {
        return R.ok(adminLogService.batchDelete(dto));
    }
}
