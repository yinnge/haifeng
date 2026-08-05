package com.haifeng.admin.controller.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.EnterprisePositionAddDTO;
import com.haifeng.admin.dto.company.EnterprisePositionBatchDeleteDTO;
import com.haifeng.admin.dto.company.EnterprisePositionQueryDTO;
import com.haifeng.admin.dto.company.EnterprisePositionUpdateDTO;
import com.haifeng.admin.service.company.EnterprisePositionService;
import com.haifeng.admin.vo.company.EnterprisePositionDetailVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 企业岗位 Controller
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/company/enterprise/{enterpriseId}/position")
@RequiredArgsConstructor
@RequireAdminModule("company_info")
public class EnterprisePositionController {

    private final EnterprisePositionService enterprisePositionService;

    /**
     * 分页查询企业岗位列表
     */
    @GetMapping("/list")
    public R<IPage<EnterprisePositionDetailVO>> list(@PathVariable Long enterpriseId,
                                                     @Valid EnterprisePositionQueryDTO dto) {
        dto.setEnterpriseId(enterpriseId);
        return R.ok(enterprisePositionService.page(dto));
    }

    /**
     * 获取岗位详情
     */
    @GetMapping("/{id}")
    @OperationLog(module = "企业管理", action = "查询岗位详情")
    public R<EnterprisePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(enterprisePositionService.detail(id));
    }

    /**
     * 新增岗位
     */
    @PostMapping
    @OperationLog(module = "企业管理", action = "新增岗位")
    public R<Long> add(@PathVariable Long enterpriseId,
                       @Valid @RequestBody EnterprisePositionAddDTO dto) {
        return R.ok(enterprisePositionService.add(enterpriseId, dto));
    }

    /**
     * 修改岗位
     */
    @PutMapping("/{id}")
    @OperationLog(module = "企业管理", action = "修改岗位")
    public R<Void> update(@PathVariable Long id,
                          @Valid @RequestBody EnterprisePositionUpdateDTO dto) {
        enterprisePositionService.update(id, dto);
        return R.ok();
    }

    /**
     * 硬删除岗位
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "企业管理", action = "硬删除岗位")
    public R<Void> delete(@PathVariable Long id) {
        enterprisePositionService.delete(id);
        return R.ok();
    }

    /**
     * 批量硬删除岗位
     */
    @PostMapping("/batch/delete")
    @OperationLog(module = "企业管理", action = "批量硬删除岗位")
    public R<Void> batchDelete(@Valid @RequestBody EnterprisePositionBatchDeleteDTO dto) {
        enterprisePositionService.batchDelete(dto.getIds());
        return R.ok();
    }
}
