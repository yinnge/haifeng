package com.haifeng.admin.controller.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.city.CityAddDTO;
import com.haifeng.admin.dto.city.CityDetailUpdateDTO;
import com.haifeng.admin.dto.city.CityQueryDTO;
import com.haifeng.admin.dto.city.CityStatusDTO;
import com.haifeng.admin.dto.city.CityUpdateDTO;
import com.haifeng.admin.service.city.CityService;
import com.haifeng.admin.vo.city.CityDetailVO;
import com.haifeng.admin.vo.city.CityListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    /**
     * 分页查询城市列表
     */
    @GetMapping("/list")
    public R<IPage<CityListVO>> list(@Valid CityQueryDTO dto) {
        return R.ok(cityService.page(dto));
    }

    /**
     * 获取城市详情（主表+详情表）
     */
    @GetMapping("/{id}")
    public R<CityDetailVO> detail(@PathVariable Long id) {
        return R.ok(cityService.detail(id));
    }

    /**
     * 新增城市（事务：主表+详情一起创建）
     */
    @PostMapping
    @OperationLog(module = "城市管理", action = "新增城市")
    public R<Long> add(@Valid @RequestBody CityAddDTO dto) {
        return R.ok(cityService.add(dto));
    }

    /**
     * 修改城市主表信息
     */
    @PutMapping("/{id}")
    @OperationLog(module = "城市管理", action = "修改城市")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CityUpdateDTO dto) {
        cityService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改城市详情表信息
     */
    @PutMapping("/{id}/detail")
    @OperationLog(module = "城市管理", action = "修改城市详情")
    public R<Void> updateDetail(@PathVariable Long id, @Valid @RequestBody CityDetailUpdateDTO dto) {
        cityService.updateDetail(id, dto);
        return R.ok();
    }

    /**
     * 修改城市状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "城市管理", action = "修改城市状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CityStatusDTO dto) {
        cityService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 硬删除城市（主表+详情表）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "城市管理", action = "硬删除城市")
    public R<Void> delete(@PathVariable Long id) {
        cityService.delete(id);
        return R.ok();
    }

    /**
     * 批量硬删除城市
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "城市管理", action = "批量硬删除城市")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        cityService.batchDelete(ids);
        return R.ok();
    }
}
