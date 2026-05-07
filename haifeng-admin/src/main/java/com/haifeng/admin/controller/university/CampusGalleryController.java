package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.*;
import com.haifeng.admin.service.university.CampusGalleryService;
import com.haifeng.admin.vo.university.CampusGalleryListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 校园图册Controller
 */
@RestController
@RequestMapping("/api/v1/admin/university/gallery")
@RequiredArgsConstructor
public class CampusGalleryController {

    private final CampusGalleryService campusGalleryService;

    /**
     * 分页查询校园图册列表
     */
    @GetMapping("/list")
    public R<IPage<CampusGalleryListVO>> list(@Valid CampusGalleryQueryDTO dto) {
        return R.ok(campusGalleryService.page(dto));
    }

    /**
     * 新增校园图册
     */
    @PostMapping
    @OperationLog(module = "院校管理", action = "新增校园图册")
    public R<Long> add(@Valid @RequestBody CampusGalleryAddDTO dto) {
        return R.ok(campusGalleryService.add(dto));
    }

    /**
     * 修改校园图册
     */
    @PutMapping("/{id}")
    @OperationLog(module = "院校管理", action = "修改校园图册")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CampusGalleryUpdateDTO dto) {
        campusGalleryService.update(id, dto);
        return R.ok();
    }

    /**
     * 删除校园图册
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "院校管理", action = "删除校园图册")
    public R<Void> delete(@PathVariable Long id) {
        campusGalleryService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除校园图册
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "院校管理", action = "批量删除校园图册")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        campusGalleryService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入校园图册数据
     */
    @PostMapping("/import")
    @OperationLog(module = "院校管理", action = "导入校园图册数据")
    public R<Void> importGallery(@RequestParam("file") MultipartFile file) {
        campusGalleryService.importGallery(file);
        return R.ok();
    }
}
