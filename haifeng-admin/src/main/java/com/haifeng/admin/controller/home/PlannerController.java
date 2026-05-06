package com.haifeng.admin.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.*;
import com.haifeng.admin.service.home.PlannerService;
import com.haifeng.admin.vo.home.*;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/home/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    /**
     * 分页查询规划师列表
     */
    @GetMapping("/list")
    public R<IPage<PlannerListVO>> list(@Valid PlannerQueryDTO dto) {
        return R.ok(plannerService.page(dto));
    }

    /**
     * 获取规划师详情
     */
    @GetMapping("/{id}")
    public R<PlannerDetailVO> detail(@PathVariable Long id) {
        return R.ok(plannerService.detail(id));
    }

    /**
     * 新增规划师
     */
    @PostMapping
    @OperationLog(module = "首页管理", action = "新增规划师")
    public R<Long> add(@Valid @RequestBody PlannerAddDTO dto) {
        return R.ok(plannerService.add(dto));
    }

    /**
     * 修改规划师
     */
    @PutMapping("/{id}")
    @OperationLog(module = "首页管理", action = "修改规划师")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PlannerUpdateDTO dto) {
        plannerService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改规划师状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "首页管理", action = "修改规划师状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        plannerService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 删除规划师
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "首页管理", action = "删除规划师")
    public R<Void> delete(@PathVariable Long id) {
        plannerService.delete(id);
        return R.ok();
    }
}
