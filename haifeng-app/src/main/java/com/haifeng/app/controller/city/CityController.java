package com.haifeng.app.controller.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.service.city.CityService;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /** 根据城市名称精确查询城市ID，需登录 */
    @RequireLogin
    @GetMapping("/id")
    public R<Long> idByName(@RequestParam @NotBlank(message = "城市名称不能为空") String name) {
        return R.ok(cityService.findIdByName(name));
    }

    /** 任务 1 接口 2：城市详情，需登录 */
    @RequireLogin
    @GetMapping("/{cityId}/detail")
    public R<CityDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long cityId) {
        return R.ok(cityService.detail(cityId));
    }
}
