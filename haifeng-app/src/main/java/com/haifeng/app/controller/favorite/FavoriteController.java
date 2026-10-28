package com.haifeng.app.controller.favorite;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.favorite.FavoriteAddDTO;
import com.haifeng.app.dto.favorite.FavoriteQueryDTO;
import com.haifeng.app.dto.favorite.FavoriteStatusQueryDTO;
import com.haifeng.app.service.favorite.FavoriteService;
import com.haifeng.app.vo.favorite.FavoriteCountsVO;
import com.haifeng.app.vo.favorite.FavoriteItemVO;
import com.haifeng.common.annotation.RateLimit;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端会员收藏
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/favorite")
@RequiredArgsConstructor
@RequireLogin
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏（幂等）
     */
    @RateLimit(value = 60, time = 60)
    @PostMapping
    public R<Void> add(@Valid @RequestBody FavoriteAddDTO dto) {
        favoriteService.add(dto);
        return R.ok();
    }

    /**
     * 取消收藏（幂等，物理删除）
     */
    @DeleteMapping("/{targetType}/{targetId}")
    public R<Void> remove(@PathVariable String targetType,
                          @PathVariable @Min(value = 1, message = "ID必须大于0") Long targetId) {
        favoriteService.remove(targetType, targetId);
        return R.ok();
    }

    /**
     * 收藏列表：targetType 不传则查询全部类型
     */
    @GetMapping("/list")
    public R<IPage<FavoriteItemVO>> list(@Valid FavoriteQueryDTO dto) {
        return R.ok(favoriteService.page(dto));
    }

    /**
     * 各类型收藏数（个人中心）
     */
    @GetMapping("/counts")
    public R<FavoriteCountsVO> counts() {
        return R.ok(favoriteService.counts());
    }

    /**
     * 批量查询某类型下已收藏的目标ID（列表页红心）
     */
    @PostMapping("/status")
    public R<List<Long>> status(@Valid @RequestBody FavoriteStatusQueryDTO dto) {
        return R.ok(favoriteService.favoritedIds(dto));
    }
}
