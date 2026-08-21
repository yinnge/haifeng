package com.haifeng.app.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.app.dto.algorithm.admission.AdmissionMajorQueryDTO;
import com.haifeng.app.service.algorithm.admission.AdmissionQueryService;
import com.haifeng.app.vo.algorithm.admission.AdmissionGroupPageVO;
import com.haifeng.app.vo.algorithm.admission.AdmissionMajorPageVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/admission")
@RequiredArgsConstructor
@RequireLogin
public class AdmissionQueryController {

    private final AdmissionQueryService admissionQueryService;

    /**
     * 分页查询专业组
     */
    @GetMapping("/group/page")
    public R<IPage<AdmissionGroupPageVO>> pageGroups(@Valid AdmissionGroupQueryDTO dto) {
        return R.ok(admissionQueryService.pageGroups(dto));
    }

    /**
     * 分页查询专业明细
     */
    @GetMapping("/major/page")
    public R<IPage<AdmissionMajorPageVO>> pageMajors(@Valid AdmissionMajorQueryDTO dto) {
        return R.ok(admissionQueryService.pageMajors(dto));
    }

    /**
     * 批量校验专业组是否仍存在于目录（用于清理前端暂存区里指向已删除/已禁用组的孤儿记录）。
     * 全局逻辑删除会自动过滤 is_deleted=true 的记录，因此返回结果只包含当前有效的组。
     */
    @GetMapping("/group/exists")
    public R<List<Integer>> listExistingGroupIds(@RequestParam("ids") List<Integer> ids) {
        return R.ok(admissionQueryService.listExistingGroupIds(ids));
    }
}
