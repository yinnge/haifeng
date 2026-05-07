package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.*;
import com.haifeng.admin.service.university.UniversityService;
import com.haifeng.admin.vo.university.UniversityDetailVO;
import com.haifeng.admin.vo.university.UniversityListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 院校管理Controller
 */
@RestController
@RequestMapping("/api/v1/admin/university")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    /**
     * 分页查询院校列表
     */
    @GetMapping("/list")
    public R<IPage<UniversityListVO>> list(@Valid UniversityQueryDTO dto) {
        return R.ok(universityService.page(dto));
    }

    /**
     * 获取院校详情
     */
    @GetMapping("/{id}")
    public R<UniversityDetailVO> detail(@PathVariable Long id) {
        return R.ok(universityService.detail(id));
    }

    /**
     * 新增院校
     */
    @PostMapping
    @OperationLog(module = "院校管理", action = "新增院校")
    public R<Long> add(@Valid @RequestBody UniversityAddDTO dto) {
        return R.ok(universityService.add(dto));
    }

    /**
     * 修改院校基础信息
     */
    @PutMapping("/{id}")
    @OperationLog(module = "院校管理", action = "修改院校基础信息")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UniversityUpdateDTO dto) {
        universityService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改院校详情信息
     */
    @PutMapping("/{id}/detail")
    @OperationLog(module = "院校管理", action = "修改院校详情信息")
    public R<Void> updateDetail(@PathVariable Long id, @Valid @RequestBody UniversityDetailUpdateDTO dto) {
        universityService.updateDetail(id, dto);
        return R.ok();
    }

    /**
     * 删除院校
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "院校管理", action = "删除院校")
    public R<Void> delete(@PathVariable Long id) {
        universityService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除院校
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "院校管理", action = "批量删除院校")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        universityService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入院校主表数据
     */
    @PostMapping("/import")
    @OperationLog(module = "院校管理", action = "导入院校主表数据")
    public R<Void> importUniversities(@RequestParam("file") MultipartFile file) {
        universityService.importUniversities(file);
        return R.ok();
    }

    /**
     * 导入院校详情数据
     */
    @PostMapping("/import-detail")
    @OperationLog(module = "院校管理", action = "导入院校详情数据")
    public R<Void> importUniversityDetails(@RequestParam("file") MultipartFile file) {
        universityService.importUniversityDetails(file);
        return R.ok();
    }
}
