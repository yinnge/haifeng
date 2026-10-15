package com.haifeng.admin.controller.fileload;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.fileload.BizArticleAddDTO;
import com.haifeng.admin.dto.fileload.BizArticleQueryDTO;
import com.haifeng.admin.dto.fileload.BizArticleUpdateDTO;
import com.haifeng.admin.service.fileload.BizArticleService;
import com.haifeng.admin.vo.fileload.BizArticleDetailVO;
import com.haifeng.admin.vo.fileload.BizArticleListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 热点资讯管理
 * <p>高考/考公/考研/中考/英语四六级五模块共用的资讯文章表（t_biz_article，见 V71），
 * 年份/省份/模块名/专栏名精准查询 + 标题模糊查询。
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/fileload/article")
@RequiredArgsConstructor
@RequireAdminModule("fileload_article")
public class BizArticleController {

    private final BizArticleService bizArticleService;

    /**
     * 新增热点资讯
     */
    @PostMapping("/add")
    @OperationLog(module = "热点资讯", action = "新增资讯")
    public R<Long> add(@Valid @RequestBody BizArticleAddDTO dto) {
        return R.ok(bizArticleService.add(dto));
    }

    /**
     * 分页查询热点资讯列表
     */
    @GetMapping("/list")
    public R<IPage<BizArticleListVO>> list(@Valid BizArticleQueryDTO dto) {
        return R.ok(bizArticleService.page(dto));
    }

    /**
     * 获取热点资讯详情（含富文本正文）
     */
    @GetMapping("/{id}")
    public R<BizArticleDetailVO> detail(@PathVariable @Min(1) Long id) {
        return R.ok(bizArticleService.detail(id));
    }

    /**
     * 修改热点资讯
     */
    @PutMapping("/{id}")
    @OperationLog(module = "热点资讯", action = "修改资讯")
    public R<Void> update(@PathVariable @Min(1) Long id,
                          @Valid @RequestBody BizArticleUpdateDTO dto) {
        bizArticleService.update(id, dto);
        return R.ok();
    }

    /**
     * 删除热点资讯
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "热点资讯", action = "删除资讯")
    public R<Void> delete(@PathVariable @Min(1) Long id) {
        bizArticleService.delete(id);
        return R.ok();
    }
}
