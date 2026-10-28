package com.haifeng.app.controller.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.service.city.CityService;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;
import com.haifeng.app.vo.city.CityRankVO;
import com.haifeng.app.vo.city.CityStatsVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C 端城市管理 - 列表/排行/概览（公开）+ 详情（登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    /** 任务 1 接口 1：分页查询城市列表，无需登录；sortBy 支持 college|keyCollege|gdp|population 倒序 */
    @GetMapping("/list")
    public R<IPage<CityListVO>> list(@Valid CityQueryDTO dto) {
        return R.ok(cityService.page(dto));
    }

    /** 城市排行榜：metric ∈ college|keyCollege|gdp|population，按指标倒序取前 limit 条，无需登录 */
    @GetMapping("/rank")
    public R<List<CityRankVO>> rank(
            @RequestParam @Pattern(regexp = "college|keyCollege|gdp|population", message = "排行榜指标不合法") String metric,
            @RequestParam(required = false) @Min(value = 1, message = "limit最小为1") @Max(value = 50, message = "limit最大为50") Integer limit) {
        return R.ok(cityService.rank(metric, limit));
    }

    /** 城市数据概览聚合：城市总数/高校总数/常住人口合计/GDP合计，无需登录 */
    @GetMapping("/stats")
    public R<CityStatsVO> stats() {
        return R.ok(cityService.stats());
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
