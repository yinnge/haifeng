package com.haifeng.app.controller.home;

import com.haifeng.app.dto.algorithm.article.BizArticleSearchDTO;
import com.haifeng.app.service.algorithm.article.BizArticleService;
import com.haifeng.app.vo.algorithm.article.BizArticleSearchItemVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端首页 - 资讯搜索（免登录公开接口）
 * <p>跨全部类型（高考/考公/考研/中考/英语四六级）的标题关键字搜索：
 * 复用 TitleAlignMatcher 去噪对齐匹配算法，返回带高亮区间的命中列表（moduleName 不限定）。
 * 搜索编排逻辑统一收敛在 BizArticleService.searchPublished（moduleName 传 null = 查全部类型），
 * 与高考模块搜索共用，避免重复。
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/home/article")
@RequiredArgsConstructor
public class BizArticleController {

    private final BizArticleService bizArticleService;

    /**
     * 跨全部类型的资讯标题搜索（免登录，去噪对齐匹配 + 高亮区间，最多 10 条，
     * 按 sort_order DESC → publish_time DESC；province 缺省/「全国」= 不限省份）
     */
    @GetMapping("/search")
    public R<List<BizArticleSearchItemVO>> search(@Valid BizArticleSearchDTO dto) {
        // moduleName 透传：null/空白 = 查全部类型，非空则仅查该模块（高考/中考/考公/考研/英语四六级）
        return R.ok(bizArticleService.searchPublished(dto.getModuleName(), dto));
    }
}
