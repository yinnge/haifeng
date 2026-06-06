package com.haifeng.app.controller.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.service.city.CityService;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端城市管理 - 列表（公开）+ 详情（登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    /** 任务 1 接口 1：分页查询城市列表，无需登录 */
    @GetMapping("/list")
    public R<IPage<CityListVO>> list(@Valid CityQueryDTO dto) {
        return R.ok(cityService.page(dto));
    }

    /** 任务 1 接口 2：城市详情，需登录 */
    @RequireLogin
    @GetMapping("/{cityId}/detail")
    public R<CityDetailVO> detail(@PathVariable Long cityId) {
        return R.ok(cityService.detail(cityId));
    }
}
