package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.CampusGalleryQueryDTO;
import com.haifeng.app.service.university.CampusGalleryService;
import com.haifeng.app.vo.university.CampusGalleryListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端校园图册（任务 4），按院校分页查询，需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class CampusGalleryController {

    private final CampusGalleryService galleryService;

    @RequireLogin
    @GetMapping("/{universityId}/gallery")
    public R<IPage<CampusGalleryListVO>> gallery(
            @PathVariable Long universityId,
            @Valid CampusGalleryQueryDTO dto) {
        return R.ok(galleryService.page(universityId, dto));
    }
}
