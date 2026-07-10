package com.haifeng.app.controller.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.service.industry.IndustryService;
import com.haifeng.app.vo.company.IndustryEnterpriseGroupVO;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端行业管理 - 列表（公开）+ 详情（登录）+ 行业相关企业（Pro）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/industry")
@RequiredArgsConstructor
public class IndustryController {

    private final IndustryService industryService;

    /** 获取所有不重复的行业分类，用于前端下拉筛选 */
    @GetMapping("/categories")
    public R<List<String>> getCategories() {
        return R.ok(industryService.getCategories());
    }

    /** 任务 2 接口 1：分页查询行业列表，无需登录 */
    @GetMapping("/list")
    public R<IPage<IndustryListVO>> list(@Valid IndustryQueryDTO dto) {
        return R.ok(industryService.page(dto));
    }

    /** 任务 2 接口 2：行业详情，需登录 */
    @RequireLogin
    @GetMapping("/{industryId}/detail")
    public R<IndustryDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long industryId) {
        return R.ok(industryService.detail(industryId));
    }

    /** 任务 6 接口：按行业 ID 列表分组查询企业，需 Pro 权限 */
    @RequirePro
    @GetMapping("/enterprises")
    public R<List<IndustryEnterpriseGroupVO>> enterprises(@RequestParam @NotEmpty(message = "行业ID列表不能为空") List<@Min(value = 1, message = "ID必须大于0") Long> industryIds) {
        return R.ok(industryService.enterprisesByIndustryIds(industryIds));
    }
}
