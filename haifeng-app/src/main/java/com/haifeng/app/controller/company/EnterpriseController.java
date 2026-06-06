package com.haifeng.app.controller.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.company.EnterpriseQueryDTO;
import com.haifeng.app.service.company.EnterpriseService;
import com.haifeng.app.vo.company.EnterpriseIndustryGroupVO;
import com.haifeng.app.vo.company.EnterpriseListVO;
import com.haifeng.app.vo.company.EnterprisePositionVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端企业管理 - 列表（公开）+ 岗位（登录）+ 关联行业（Pro）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    /** 企业分页列表（公开） */
    @GetMapping("/list")
    public R<IPage<EnterpriseListVO>> list(@Valid EnterpriseQueryDTO dto) {
        return R.ok(enterpriseService.page(dto));
    }

    /** 企业岗位列表（登录） */
    @RequireLogin
    @GetMapping("/{enterpriseId}/positions")
    public R<List<EnterprisePositionVO>> positions(@PathVariable Long enterpriseId) {
        return R.ok(enterpriseService.positions(enterpriseId));
    }

    /** 企业 → 行业跳转信息（Pro 及以上） */
    @RequirePro
    @GetMapping("/industries")
    public R<List<EnterpriseIndustryGroupVO>> industries(@RequestParam List<Long> enterpriseIds) {
        return R.ok(enterpriseService.industriesByEnterpriseIds(enterpriseIds));
    }
}
