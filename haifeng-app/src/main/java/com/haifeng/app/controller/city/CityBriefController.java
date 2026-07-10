package com.haifeng.app.controller.city;

import com.haifeng.app.service.city.CityService;
import com.haifeng.app.vo.city.CityBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/city")
@RequiredArgsConstructor
public class CityBriefController {

    private final CityService cityService;

    @RequireLogin
    @GetMapping("/brief")
    public R<CityBriefVO> getByName(
            @RequestParam @NotBlank(message = "城市名称不能为空") @Size(max = 50, message = "城市名称长度不能超过50") String name) {
        return R.ok(cityService.getBriefByName(name));
    }
}
