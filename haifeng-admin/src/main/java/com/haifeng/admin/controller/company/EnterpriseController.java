package com.haifeng.admin.controller.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.EnterpriseAddDTO;
import com.haifeng.admin.dto.company.EnterpriseBatchDeleteDTO;
import com.haifeng.admin.dto.company.EnterpriseQueryDTO;
import com.haifeng.admin.dto.company.EnterpriseStatusDTO;
import com.haifeng.admin.dto.company.EnterpriseUpdateDTO;
import com.haifeng.admin.service.company.EnterpriseService;
import com.haifeng.admin.vo.company.EnterpriseDetailVO;
import com.haifeng.admin.vo.company.EnterpriseListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 企业管理 Controller
 */
@RestController
@RequestMapping("/api/v1/admin/company/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    /**
     * 分页查询企业列表
     */
    @GetMapping("/list")
    public R<IPage<EnterpriseListVO>> list(@Valid EnterpriseQueryDTO dto) {
        return R.ok(enterpriseService.page(dto));
    }

    /**
     * 获取企业详情
     */
    @GetMapping("/{id}")
    public R<EnterpriseDetailVO> detail(@PathVariable Long id) {
        return R.ok(enterpriseService.detail(id));
    }

    /**
     * 新增企业
     */
    @PostMapping
    @OperationLog(module = "企业管理", action = "新增企业")
    public R<Long> add(@Valid @RequestBody EnterpriseAddDTO dto) {
        return R.ok(enterpriseService.add(dto));
    }

    /**
     * 修改企业
     */
    @PutMapping("/{id}")
    @OperationLog(module = "企业管理", action = "修改企业")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody EnterpriseUpdateDTO dto) {
        enterpriseService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改企业状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "企业管理", action = "修改企业状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody EnterpriseStatusDTO dto) {
        enterpriseService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 硬删除企业
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "企业管理", action = "硬删除企业")
    public R<Void> delete(@PathVariable Long id) {
        enterpriseService.delete(id);
        return R.ok();
    }

    /**
     * 批量硬删除企业
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "企业管理", action = "批量硬删除企业")
    public R<Void> batchDelete(@Valid @RequestBody EnterpriseBatchDeleteDTO dto) {
        enterpriseService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入企业
     */
    @PostMapping("/import")
    @OperationLog(module = "企业管理", action = "导入企业")
    public R<Void> importEnterprises(@RequestParam("file") MultipartFile file) {
        enterpriseService.importEnterprises(file);
        return R.ok();
    }
}
