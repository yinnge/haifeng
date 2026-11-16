package com.haifeng.admin.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.ConsultationQueryDTO;
import com.haifeng.admin.service.home.ConsultationService;
import com.haifeng.admin.vo.home.ConsultationDetailVO;
import com.haifeng.admin.vo.home.ConsultationListVO;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页管理 - 咨询留言（只读）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/home/consultation")
@RequiredArgsConstructor
@RequireAdminModule("home_consultation")
public class ConsultationController {

    private final ConsultationService consultationService;

    /** 分页查询咨询留言 */
    @GetMapping("/list")
    public R<IPage<ConsultationListVO>> list(@Valid ConsultationQueryDTO dto) {
        return R.ok(consultationService.page(dto));
    }

    /** 留言详情 */
    @GetMapping("/{id}")
    public R<ConsultationDetailVO> detail(@Min(value = 1, message = "ID必须大于0") @PathVariable Long id) {
        return R.ok(consultationService.detail(id));
    }
}
