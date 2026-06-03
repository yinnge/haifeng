package com.haifeng.app.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.AnnouncementQueryDTO;
import com.haifeng.app.service.home.AnnouncementService;
import com.haifeng.app.vo.home.AnnouncementDetailVO;
import com.haifeng.app.vo.home.AnnouncementListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端首页 - 公告（公开接口，无需登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/home/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /** 分页查询公告列表 */
    @GetMapping
    public R<IPage<AnnouncementListVO>> list(@Valid AnnouncementQueryDTO dto) {
        return R.ok(announcementService.page(dto));
    }

    /** 公告详情 */
    @GetMapping("/{id}")
    public R<AnnouncementDetailVO> detail(@PathVariable Long id) {
        return R.ok(announcementService.detail(id));
    }
}
