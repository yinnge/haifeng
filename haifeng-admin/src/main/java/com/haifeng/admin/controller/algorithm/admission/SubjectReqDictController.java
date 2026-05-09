package com.haifeng.admin.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictAddDTO;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictQueryDTO;
import com.haifeng.admin.service.algorithm.admission.SubjectReqDictService;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictDetailVO;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/admission/subject-req")
@RequiredArgsConstructor
public class SubjectReqDictController {

    private final SubjectReqDictService subjectReqDictService;

    @GetMapping("/page")
    public R<IPage<SubjectReqDictListVO>> page(@Valid SubjectReqDictQueryDTO dto) {
        return R.ok(subjectReqDictService.page(dto));
    }

    @GetMapping("/{id}")
    public R<SubjectReqDictDetailVO> detail(@PathVariable Integer id) {
        return R.ok(subjectReqDictService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "选科要求管理", action = "新增选科要求")
    public R<Integer> add(@Valid @RequestBody SubjectReqDictAddDTO dto) {
        return R.ok(subjectReqDictService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "选科要求管理", action = "修改选科要求")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody SubjectReqDictAddDTO dto) {
        subjectReqDictService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "选科要求管理", action = "删除选科要求")
    public R<Void> delete(@PathVariable Integer id) {
        subjectReqDictService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "选科要求管理", action = "批量删除选科要求")
    public R<Void> batchDelete(@RequestBody List<Integer> ids) {
        subjectReqDictService.batchDelete(ids);
        return R.ok();
    }
}
