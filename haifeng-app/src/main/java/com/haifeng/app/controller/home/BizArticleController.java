package com.haifeng.app.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.article.BizArticleGlobalQueryDTO;
import com.haifeng.app.dto.algorithm.article.BizArticleSearchDTO;
import com.haifeng.app.service.algorithm.article.BizArticleService;
import com.haifeng.app.vo.algorithm.article.BizArticleDetailVO;
import com.haifeng.app.vo.algorithm.article.BizArticleGlobalListVO;
import com.haifeng.app.vo.algorithm.article.BizArticleNewsVO;
import com.haifeng.app.vo.algorithm.article.BizArticleSearchItemVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端首页 / 全局资讯（免登录公开接口，base=/api/v1/app/home/article，已纳入 SecurityConfig 白名单 /api/v1/app/home/**）
 * <p>列表 / 详情：跨全部模块（高考/中考/考公/考研/英语四六级）统一入口，
 * moduleName 精准查询可空（空=全部模块）、province 可空（空/「全国」= 不限省份），均由前端传入。
 * 搜索：跨全部类型的标题关键字搜索（见 /search，复用 TitleAlignMatcher，带高亮区间）。
 * 主页资讯：轻量列表，只返回 标题/专栏名/年份。
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/home/article")
@RequiredArgsConstructor
public class BizArticleController {

    private final BizArticleService bizArticleService;

    /**
     * 跨模块全局资讯列表（免登录）：moduleName 精准查询可空、province 可空；
     * 返回 模块名/省份/专栏名/标题/年份/sortOrder（与高考列表近似，额外带模块名）。
     */
    @GetMapping("/list")
    public R<IPage<BizArticleGlobalListVO>> list(@Valid BizArticleGlobalQueryDTO dto) {
        return R.ok(bizArticleService.pagePublishedGlobal(dto));
    }

    /**
     * 跨模块资讯详情（免登录，不限定模块，id 唯一；下架/不存在一律 404）。
     */
    @GetMapping("/{id}")
    public R<BizArticleDetailVO> detail(@PathVariable @Min(1) Long id) {
        return R.ok(bizArticleService.getPublishedDetailGlobal(id));
    }

    /**
     * 主页资讯列表（免登录，轻量 VO：标题/专栏名/年份；按年份倒序，NULL 置后）。
     */
    @GetMapping("/news")
    public R<IPage<BizArticleNewsVO>> news(@Valid BizArticleGlobalQueryDTO dto) {
        return R.ok(bizArticleService.pagePublishedNews(dto));
    }

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
