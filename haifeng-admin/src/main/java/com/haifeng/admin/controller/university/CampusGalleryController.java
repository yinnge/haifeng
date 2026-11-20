package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.university.*;
import com.haifeng.admin.service.university.CampusGalleryService;
import com.haifeng.admin.vo.common.ImageUploadVO;
import com.haifeng.admin.vo.university.CampusGalleryDetailVO;
import com.haifeng.admin.vo.university.CampusGalleryListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.resource.OssService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * 校园图册Controller
 */
@RestController
@RequestMapping("/api/v1/admin/university/gallery")
@RequiredArgsConstructor
@RequireAdminModule("university_album")
@Validated
public class CampusGalleryController {

    private final CampusGalleryService campusGalleryService;
    private final OssService ossService;

    /** 允许的图片扩展名（小写，不含点） */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "svg");

    /** 单张图片大小上限：2MB */
    private static final long MAX_SIZE = 2L * 1024 * 1024;

    /**
     * 分页查询校园图册列表
     */
    @GetMapping("/list")
    public R<IPage<CampusGalleryListVO>> list(@Valid CampusGalleryQueryDTO dto) {
        return R.ok(campusGalleryService.page(dto));
    }

    /**
     * 获取校园图册详情
     */
    @GetMapping("/{id}")
    public R<CampusGalleryDetailVO> detail(@PathVariable Long id) {
        return R.ok(campusGalleryService.detail(id));
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
     * 上传校园图册图片（imageUrl）
     * <p>写入 universities-album 公共读 bucket，返回可直接访问的完整 URL。
     * 前端仍可手填 URL，两种方式并存。
     */
    @PostMapping("/upload-image")
    @OperationLog(module = "院校管理", action = "上传校园图册图片")
    public R<ImageUploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
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
        String url = ossService.uploadUniversityAlbumImage(content, "." + extension);
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
     * 修改校园图册
     */
    @PutMapping("/{id}")
    @OperationLog(module = "院校管理", action = "修改校园图册")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CampusGalleryUpdateDTO dto) {
        campusGalleryService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改校园图册状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "院校管理", action = "修改校园图册状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        campusGalleryService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    /**
     * 软删除校园图册（可恢复）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "院校管理", action = "软删除校园图册")
    public R<Void> delete(@PathVariable Long id) {
        campusGalleryService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除校园图册（永久删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "院校管理", action = "硬删除校园图册")
    public R<Void> hardDelete(@PathVariable Long id) {
        campusGalleryService.hardDelete(id);
        return R.ok();
    }

    /**
     * 批量软删除校园图册
     */
    @PostMapping("/batch-delete")
    @OperationLog(module = "院校管理", action = "批量软删除校园图册")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        campusGalleryService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 批量硬删除校园图册
     */
    @PostMapping("/batch-hard-delete")
    @OperationLog(module = "院校管理", action = "批量硬删除校园图册")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        campusGalleryService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入校园图册数据
     */
    @PostMapping("/import")
    @OperationLog(module = "院校管理", action = "导入校园图册数据")
    public R<ImportResultVO> importGallery(@RequestParam("file") MultipartFile file) {
        return R.ok(campusGalleryService.importGallery(file));
    }
}
