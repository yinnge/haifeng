package com.haifeng.app.controller.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.resource.ResourceQueryDTO;
import com.haifeng.app.service.resource.ResourceService;
import com.haifeng.app.vo.resource.ResourceListVO;
import com.haifeng.app.vo.resource.ResourceUrlVO;

import java.util.List;
import com.haifeng.common.annotation.RequireLogin;
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
 * C 端资源管理 - 列表（公开）+ URL（登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /** 任务 3 接口 1：分页查询资源列表，无需登录 */
    @GetMapping("/list")
    public R<IPage<ResourceListVO>> list(@Valid ResourceQueryDTO dto) {
        return R.ok(resourceService.page(dto));
    }

    /** 任务 3 接口 2 & 3：查看资源 URL 并同步 +1 浏览计数，需登录 */
    @RequireLogin
    @GetMapping("/{id}/url")
    public R<ResourceUrlVO> getUrl(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(resourceService.getUrl(id));
    }

    /** 获取所有不重复的分类（用于前端下拉筛选） */
    @GetMapping("/categories")
    public R<List<String>> getCategories() {
        return R.ok(resourceService.getCategories());
    }
}
