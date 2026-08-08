package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.GrassrootsProjectPositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/project")
@RequiredArgsConstructor
public class GrassrootsProjectPositionController {

    private final GrassrootsProjectPositionService grassrootsProjectPositionService;

    @GetMapping("/list")
    public R<IPage<GrassrootsProjectPositionListVO>> list(@Valid GrassrootsProjectPositionSearchDTO dto) {
        return R.ok(grassrootsProjectPositionService.page(dto));
    }

    /** 所有不重复的招募年份（倒序），供前端年份筛选下拉 */
    @GetMapping("/years")
    public R<List<String>> years() {
        return R.ok(grassrootsProjectPositionService.listYears());
    }

    /** 所有不重复的毕业年份要求（倒序），供前端毕业年份筛选下拉 */
    @GetMapping("/grad-years")
    public R<List<String>> gradYears() {
        return R.ok(grassrootsProjectPositionService.listGradYears());
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<GrassrootsProjectPositionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(grassrootsProjectPositionService.detail(id));
    }
}
