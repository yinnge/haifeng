package com.haifeng.admin.controller.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.city.CityAddDTO;
import com.haifeng.admin.dto.city.CityDetailUpdateDTO;
import com.haifeng.admin.dto.city.CityQueryDTO;
import com.haifeng.admin.dto.city.CityStatusDTO;
import com.haifeng.admin.dto.city.CityUpdateDTO;
import com.haifeng.admin.service.city.CityService;
import com.haifeng.admin.vo.common.ImageUploadVO;
import com.haifeng.admin.vo.city.CityDetailVO;
import com.haifeng.admin.vo.city.CityListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.resource.OssService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/api/v1/admin/city")
@RequiredArgsConstructor
@RequireAdminModule("city_info")
public class CityController {

    private final CityService cityService;
    private final OssService ossService;

    /** 允许的图片扩展名（小写，不含点） */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "svg");

    /** 单张图片大小上限：2MB */
    private static final long MAX_SIZE = 2L * 1024 * 1024;

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
    @OperationLog(module = "城市管理", action = "查看城市详情")
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
     * 上传城市封面图（coverImageUrl）
     * <p>写入 city-cover-image 公共读 bucket，返回可直接访问的完整 URL。
     * 前端仍可手填 URL，两种方式并存。
     */
    @PostMapping("/upload-cover")
    @OperationLog(module = "城市管理", action = "上传城市封面图")
    public R<ImageUploadVO> uploadCover(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片大小不能超过 2MB");
        }
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "仅支持 png / jpg / jpeg / webp / svg 格式的图片");
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片读取失败");
        }
        if ("svg".equals(extension)) {
            content = OssService.sanitizeSvg(content);
        }
        String url = ossService.uploadCityCoverImage(content, "." + extension);
        return R.ok(new ImageUploadVO(url, originalName, (long) content.length));
    }

    /** 从文件名提取小写扩展名（不含点），无扩展名返回空串 */
    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
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
    @PostMapping("/batch/delete")
    @OperationLog(module = "城市管理", action = "批量硬删除城市")
    public R<Void> batchDelete(@Valid @Size(min = 1, max = 200, message = "删除数量必须在1-200条之间") @RequestBody List<Long> ids) {
        cityService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 导入城市主表xlsx
     */
    @PostMapping("/import")
    @OperationLog(module = "城市管理", action = "导入城市主表")
    public R<Void> importCities(@RequestParam("file") MultipartFile file) {
        validateImportFile(file);
        cityService.importCities(file);
        return R.ok();
    }

    /**
     * 导入城市详情xlsx（多Sheet）
     */
    @PostMapping("/import-detail")
    @OperationLog(module = "城市管理", action = "导入城市详情")
    public R<Void> importCityDetails(@RequestParam("file") MultipartFile file) {
        validateImportFile(file);
        cityService.importCityDetails(file);
        return R.ok();
    }

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "导入文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || contentType.equals("application/vnd.ms-excel"))) {
            throw new BusinessException(400, "仅支持.xlsx格式的Excel文件");
        }
    }
}
