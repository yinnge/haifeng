package com.haifeng.app.controller.employment.contentManagement.notice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/content/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<NoticeDetailVO>> list(@Valid NoticeQueryDTO dto) {
        return R.ok(noticeService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<NoticeDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(noticeService.detail(id));
    }

    @GetMapping("/list-by-type")
    public R<List<NoticeDetailVO>> listByType(
            @RequestParam @NotBlank @Size(max = 50) String noticeCategory,
            @RequestParam(defaultValue = "招聘公告") @Size(max = 20) String noticeType) {
            return R.ok(noticeService.listByCategoryAndType(noticeCategory, noticeType));
    }

    @GetMapping("/{id}/view")
    public R<Void> view(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        noticeService.incrementViewCount(id);
        return R.ok();
    }
}
