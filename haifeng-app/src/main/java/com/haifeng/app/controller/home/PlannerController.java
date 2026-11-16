package com.haifeng.app.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.service.home.PlannerService;
import com.haifeng.app.vo.home.PlannerAppointmentVO;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端首页 - 规划师（公开接口，无需登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/home/planners")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    /** 该服务对象下实际存在的省份（供前端筛选按钮，DB 有什么返回什么） */
    @GetMapping("/provinces")
    public R<List<String>> provinces(
            @NotBlank(message = "服务对象不能为空") @RequestParam String serviceTarget) {
        return R.ok(plannerService.listProvinces(serviceTarget));
    }

    /** 分页查询规划师列表 */
    @GetMapping
    public R<IPage<PlannerListVO>> list(@Valid PlannerQueryDTO dto) {
        return R.ok(plannerService.page(dto));
    }

    /** 规划师详情（基础信息 + 列表页展示 + 详情页展示） */
    @GetMapping("/{id}")
    public R<PlannerDetailVO> detail(@Min(value = 1, message = "ID必须大于0") @PathVariable Long id) {
        return R.ok(plannerService.detail(id));
    }

    /** 规划师预约信息（头像 + 姓名 + 联系方式） */
    @GetMapping("/{id}/appointment")
    public R<PlannerAppointmentVO> appointment(@Min(value = 1, message = "ID必须大于0") @PathVariable Long id) {
        return R.ok(plannerService.appointment(id));
    }
}
