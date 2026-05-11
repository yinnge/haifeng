package com.haifeng.admin.controller.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.EnterpriseIndustryBatchDeleteDTO;
import com.haifeng.admin.dto.company.EnterpriseIndustryQueryDTO;
import com.haifeng.admin.service.company.EnterpriseIndustryService;
import com.haifeng.admin.vo.company.EnterpriseIndustryDetailVO;
import com.haifeng.admin.vo.company.EnterpriseIndustryListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 企业-行业关联 Controller
 */
@RestController
@RequestMapping("/api/v1/admin/company/enterprise-industry")
@RequiredArgsConstructor
public class EnterpriseIndustryController {

    private final EnterpriseIndustryService enterpriseIndustryService;

    /**
     * 分页查询企业-行业关联列表
     */
    @GetMapping("/list")
    public R<IPage<EnterpriseIndustryListVO>> list(@Valid EnterpriseIndustryQueryDTO dto) {
        return R.ok(enterpriseIndustryService.page(dto));
    }

    /**
     * 获取关联详情
     */
    @GetMapping("/{id}")
    public R<EnterpriseIndustryDetailVO> detail(@PathVariable Long id) {
        return R.ok(enterpriseIndustryService.detail(id));
    }

    /**
     * 硬删除关联
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "企业-行业关联", action = "硬删除关联")
    public R<Void> delete(@PathVariable Long id) {
        enterpriseIndustryService.delete(id);
        return R.ok();
    }

    /**
     * 批量硬删除关联
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "企业-行业关联", action = "批量硬删除关联")
    public R<Void> batchDelete(@Valid @RequestBody EnterpriseIndustryBatchDeleteDTO dto) {
        enterpriseIndustryService.batchDelete(dto);
        return R.ok();
    }

    /**
     * 导入企业-行业关联
     */
    @PostMapping("/import")
    @OperationLog(module = "企业-行业关联", action = "导入关联")
    public R<Void> importEnterpriseIndustries(@RequestParam("file") MultipartFile file) {
        enterpriseIndustryService.importEnterpriseIndustries(file);
        return R.ok();
    }
}
