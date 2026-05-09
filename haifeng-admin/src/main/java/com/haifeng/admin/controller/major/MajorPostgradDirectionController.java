package com.haifeng.admin.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.major.MajorPostgradDirectionAddDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionQueryDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionUpdateDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.service.major.MajorPostgradDirectionService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionDetailVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/major-postgrad-direction")
@RequiredArgsConstructor
public class MajorPostgradDirectionController {

    private final MajorPostgradDirectionService majorPostgradDirectionService;

    /**
     * 分页查询本科专业-考研方向关联列表
     */
    @GetMapping("/list")
    public R<IPage<MajorPostgradDirectionListVO>> list(@Valid MajorPostgradDirectionQueryDTO dto) {
        return R.ok(majorPostgradDirectionService.list(dto));
    }

    /**
     * 获取关联详情
     */
    @GetMapping("/{id}")
    public R<MajorPostgradDirectionDetailVO> getDetail(@PathVariable Long id) {
        return R.ok(majorPostgradDirectionService.getDetail(id));
    }

    /**
     * 新增关联
     */
    @PostMapping("/add")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "新增关联")
    public R<Void> add(@Valid @RequestBody MajorPostgradDirectionAddDTO dto) {
        majorPostgradDirectionService.add(dto);
        return R.ok();
    }

    /**
     * 修改关联
     */
    @PutMapping("/{id}")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "修改关联")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MajorPostgradDirectionUpdateDTO dto) {
        majorPostgradDirectionService.update(id, dto);
        return R.ok();
    }

    /**
     * 删除关联（硬删除）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "删除关联")
    public R<Void> delete(@PathVariable Long id) {
        majorPostgradDirectionService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除关联（硬删除）
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "批量删除关联")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        majorPostgradDirectionService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入本科专业-考研方向关联数据
     */
    @PostMapping("/import")
    @OperationLog(module = "本科专业-考研方向关联管理", action = "导入关联数据")
    public R<ImportResultVO> importData(@RequestParam("file") MultipartFile file) {
        return R.ok(majorPostgradDirectionService.importData(file));
    }
}
