package com.haifeng.app.controller.city;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.vo.city.CityBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/city")
@RequiredArgsConstructor
@RequireLogin
public class CityBriefController {

    private final CityMapper cityMapper;

    @GetMapping("/brief")
    public R<CityBriefVO> getByName(@RequestParam String name) {
        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(City::getCityName, name)
               .eq(City::getIsDeleted, false);

        City city = cityMapper.selectOne(wrapper);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        return R.ok(CityBriefVO.builder()
                .cityName(city.getCityName())
                .province(city.getProvince())
                .region(city.getRegion())
                .cityIntro(city.getCityIntro())
                .collegeCount(city.getCollegeCount())
                .build());
    }
}
