package com.haifeng.admin.controller.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.industry.IndustryAddDTO;
import com.haifeng.admin.dto.industry.IndustryDetailUpdateDTO;
import com.haifeng.admin.dto.industry.IndustryQueryDTO;
import com.haifeng.admin.dto.industry.IndustryStatusDTO;
import com.haifeng.admin.dto.industry.IndustryUpdateDTO;
import com.haifeng.admin.service.industry.IndustryService;
import com.haifeng.admin.vo.industry.IndustryDetailVO;
import com.haifeng.admin.vo.industry.IndustryListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/industry")
@RequiredArgsConstructor
public class IndustryController {

    private final IndustryService industryService;

    /**
     * 分页查询行业列表
     */
    @GetMapping("/list")
    public R<IPage<IndustryListVO>> list(@Valid IndustryQueryDTO dto) {
        return R.ok(industryService.page(dto));
    }

    /**
     * 获取行业详情（主表+详情表）
     */
    @GetMapping("/{id}")
    public R<IndustryDetailVO> detail(@PathVariable Long id) {
        return R.ok(industryService.detail(id));
    }

    /**
     * 新增行业（事务：主表+详情一起创建）
     */
    @PostMapping
    @OperationLog(module = "行业管理", action = "新增行业")
    public R<Long> add(@Valid @RequestBody IndustryAddDTO dto) {
        return R.ok(industryService.add(dto));
    }

    /**
     * 修改行业主表信息
     */
    @PutMapping("/{id}")
    @OperationLog(module = "行业管理", action = "修改行业")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody IndustryUpdateDTO dto) {
        industryService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改行业详情表信息
     */
    @PutMapping("/{id}/detail")
    @OperationLog(module = "行业管理", action = "修改行业详情")
    public R<Void> updateDetail(@PathVariable Long id, @Valid @RequestBody IndustryDetailUpdateDTO dto) {
        industryService.updateDetail(id, dto);
        return R.ok();
    }

    /**
     * 修改行业状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "行业管理", action = "修改行业状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody IndustryStatusDTO dto) {
        industryService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 硬删除行业（主表+详情表）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "行业管理", action = "硬删除行业")
    public R<Void> delete(@PathVariable Long id) {
        industryService.delete(id);
        return R.ok();
    }

    /**
     * 批量硬删除行业
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "行业管理", action = "批量硬删除行业")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        industryService.batchDelete(ids);
        return R.ok();
    }
}
