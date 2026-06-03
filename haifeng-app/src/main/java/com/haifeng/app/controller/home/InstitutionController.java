package com.haifeng.app.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.service.home.InstitutionService;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端首页 - 培训机构（公开接口，无需登录）
 */
@RestController
@RequestMapping("/api/v1/app/home/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    /** 分页查询培训机构列表 */
    @GetMapping
    public R<IPage<InstitutionListVO>> list(@Valid InstitutionQueryDTO dto) {
        return R.ok(institutionService.page(dto));
    }

    /** 培训机构详情 */
    @GetMapping("/{id}")
    public R<InstitutionDetailVO> detail(@PathVariable Long id) {
        return R.ok(institutionService.detail(id));
    }
}
