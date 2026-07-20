package com.haifeng.app.controller.employment.contentManagement.notice;

import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.common.response.R;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/list-by-type")
    public R<List<NoticeDetailVO>> listByType(
            @RequestParam @NotBlank @Size(max = 50) String noticeCategory,
            @RequestParam(defaultValue = "招聘公告") @Size(max = 20) String noticeType) {
            return R.ok(noticeService.listByCategoryAndType(noticeCategory, noticeType));
    }

}
}
