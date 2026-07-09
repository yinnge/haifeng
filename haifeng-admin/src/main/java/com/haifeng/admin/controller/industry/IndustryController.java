package com.haifeng.admin.controller.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.industry.IndustryAddDTO;
import com.haifeng.admin.dto.industry.IndustryBatchDeleteDTO;
import com.haifeng.admin.dto.industry.IndustryDetailUpdateDTO;
import com.haifeng.admin.dto.industry.IndustryQueryDTO;
import com.haifeng.admin.dto.industry.IndustryStatusDTO;
import com.haifeng.admin.dto.industry.IndustryUpdateDTO;
import com.haifeng.admin.service.industry.IndustryService;
import com.haifeng.admin.vo.industry.IndustryDetailVO;
import com.haifeng.admin.vo.industry.IndustryListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 行业管理 - 行业主表与详情表增删改查，支持主表xlsx和详情xlsx（含9个Sheet）的导入
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/industry")
@RequiredArgsConstructor
@RequireAdminModule("industry_info")
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
    @OperationLog(module = "行业管理", action = "查询行业详情")
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
    @PostMapping("/batch/delete")
    @OperationLog(module = "行业管理", action = "批量硬删除行业")
    public R<Void> batchDelete(@Valid @RequestBody IndustryBatchDeleteDTO dto) {
        industryService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入行业主表xlsx
     */
    @PostMapping("/import")
    @OperationLog(module = "行业管理", action = "导入行业主表")
    public R<Void> importIndustries(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.fail(400, "请上传文件");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".xlsx")) {
            return R.fail(400, "仅支持 .xlsx 格式文件");
        }
        industryService.importIndustries(file);
        return R.ok();
    }

    /**
     * 导入行业详情xlsx（9个Sheet）
     */
    @PostMapping("/import-detail")
    @OperationLog(module = "行业管理", action = "导入行业详情")
    public R<Void> importIndustryDetails(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.fail(400, "请上传文件");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".xlsx")) {
            return R.fail(400, "仅支持 .xlsx 格式文件");
        }
        industryService.importIndustryDetails(file);
        return R.ok();
    }
}
