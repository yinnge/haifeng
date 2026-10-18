package com.haifeng.app.controller.algorithm.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.article.BizArticleQueryDTO;
import com.haifeng.app.dto.algorithm.article.BizArticleSearchDTO;
import com.haifeng.app.service.algorithm.article.BizArticleService;
import com.haifeng.app.vo.algorithm.article.BizArticleDetailVO;
import com.haifeng.app.vo.algorithm.article.BizArticleListVO;
import com.haifeng.app.vo.algorithm.article.BizArticleSearchItemVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端 - 高考热点资讯（免登录公开接口）
 * <p>列表：精准省份（取自用户高考档案，前端档案变更时重新拉取）+ 模块固定「高考」+ status=0；
 * 返回全部专栏的资讯（不做专栏过滤），前端根据 columnName 自行归类排版。
 * 详情：含富文本正文。
 * 搜索：标题关键字去噪对齐匹配（免登录）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/article/gaokao")
@RequiredArgsConstructor
public class GaokaoArticleController {

    private static final String MODULE_NAME = "高考";

    private final BizArticleService bizArticleService;

    /**
     * 分页查询高考热点资讯列表（免登录）
     */
    @GetMapping("/list")
    public R<IPage<BizArticleListVO>> list(@Valid BizArticleQueryDTO dto) {
        return R.ok(bizArticleService.pagePublished(MODULE_NAME, dto));
    }

    /**
     * 高考热点资讯详情（免登录）
     */
    @GetMapping("/{id}")
    public R<BizArticleDetailVO> detail(@PathVariable @Min(1) Long id) {
        return R.ok(bizArticleService.getPublishedDetail(MODULE_NAME, id));
    }

    /**
     * 标题关键字搜索（免登录，去噪对齐匹配 + 高亮区间，最多 10 条，
     * 按 sort_order DESC → publish_time DESC；province 缺省/「全国」= 不限省份）
     */
    @GetMapping("/search")
    public R<List<BizArticleSearchItemVO>> search(@Valid BizArticleSearchDTO dto) {
        return R.ok(bizArticleService.searchPublished(MODULE_NAME, dto));
    }
}
