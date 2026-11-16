package com.haifeng.app.controller.consultation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.consultation.ConsultationQueryDTO;
import com.haifeng.app.service.consultation.ConsultationService;
import com.haifeng.app.vo.consultation.ConsultationDetailVO;
import com.haifeng.app.vo.consultation.ConsultationListVO;
import com.haifeng.app.vo.consultation.ConsultationUnhandledCountVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequireTeacher;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端教师收件箱 - 咨询留言（仅老师身份可访问）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/teacher/consultations")
@RequiredArgsConstructor
@RequireLogin
@RequireTeacher
public class TeacherConsultationController {

    private final ConsultationService consultationService;

    /** 我收到的留言（page/size/status/bizType） */
    @GetMapping
    public R<IPage<ConsultationListVO>> list(@Valid ConsultationQueryDTO dto) {
        return R.ok(consultationService.page(dto));
    }

    /** 未处理留言数（角标） */
    @GetMapping("/unhandled-count")
    public R<ConsultationUnhandledCountVO> unhandledCount() {
        return R.ok(consultationService.unhandledCount());
    }

    /** 留言详情，非本人留言返回 404 */
    @GetMapping("/{id}")
    public R<ConsultationDetailVO> detail(@Min(value = 1, message = "ID必须大于0") @PathVariable Long id) {
        return R.ok(consultationService.detail(id));
    }

    /** 标记已联系，重复调用幂等 */
    @PutMapping("/{id}/handled")
    public R<Void> markHandled(@Min(value = 1, message = "ID必须大于0") @PathVariable Long id) {
        consultationService.markHandled(id);
        return R.ok();
    }
}
