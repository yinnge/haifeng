package com.haifeng.app.controller.home;

import com.haifeng.app.service.home.SiteService;
import com.haifeng.app.vo.home.SiteInfoVO;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/home")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    /**
     * 获取站点信息（公开接口，无需登录）
     */
    @GetMapping("/site-info")
    public R<SiteInfoVO> getSiteInfo() {
        return R.ok(siteService.getSiteInfo());
    }
}
