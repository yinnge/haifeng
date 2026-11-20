package com.haifeng.admin.controller.common;

import com.haifeng.admin.vo.common.ImageUploadVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.resource.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 管理端 - 通用工具接口（图片上传等）
 *
 * <p>本 Controller 不绑定 {@code @RequireAdminModule}：图片上传是多个业务模块共用的基础能力，
 * 若绑定单个模块码会导致「有 A 模块权限但无 B 模块权限」的管理员无法在其他模块上传图片。
 * 这里统一用 {@code @RequireLogin(userType = "admin")} 限定为管理员身份。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/common")
@RequiredArgsConstructor
@RequireLogin(userType = "admin")
public class AdminCommonController {

    /** 允许的图片扩展名（小写，不含点） */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "svg");

    /** 单张图片大小上限：2MB */
    private static final long MAX_SIZE = 2L * 1024 * 1024;

    private final OssService ossService;

    /**
     * 上传通用公开素材图片（objectKey 前缀 haifeng/images/）
     *
     * <p>图片写入独立的「公共读」bucket，返回永久有效的完整 URL，
     * 可直接被 C 端 {@code <img src>} 引用（规划师头像等）。
     * 私有 bucket 承载 VIP 门禁文件，绝不复用。
     */
    @PostMapping("/upload")
    @OperationLog(module = "通用工具", action = "上传图片")
    public R<ImageUploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        ValidatedImage image = validateAndRead(file);
        String url = ossService.uploadPublicBytes(image.content(), "." + image.extension());
        log.info("上传图片成功: url={}, size={}", url, image.content().length);
        return R.ok(new ImageUploadVO(url, image.originalName(), (long) image.content().length));
    }

    /**
     * 上传培训机构图片：与通用素材同一个公共读 bucket，仅 objectKey 前缀改为 haifeng/institutions/。
     *
     * <p>该前缀与 V83 种子数据（qihang-*.svg / shangan-*.svg 等）一致，
     * OSS 无真实目录，因此新旧图片在控制台的 institutions「文件夹」下共存，无需新增 bucket。
     */
    @PostMapping("/upload-institution")
    @OperationLog(module = "通用工具", action = "上传机构图片")
    public R<ImageUploadVO> uploadInstitutionImage(@RequestParam("file") MultipartFile file) {
        ValidatedImage image = validateAndRead(file);
        String url = ossService.uploadInstitutionImage(image.content(), "." + image.extension());
        log.info("上传机构图片成功: url={}, size={}", url, image.content().length);
        return R.ok(new ImageUploadVO(url, image.originalName(), (long) image.content().length));
    }

    /** 上传前统一校验：非空、大小、扩展名白名单、SVG 消毒 */
    private ValidatedImage validateAndRead(MultipartFile file) {
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

        // SVG 可内嵌脚本，上传到公共读 bucket 等于对外发布可执行内容，必须先消毒
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片读取失败");
        }
        if ("svg".equals(extension)) {
            content = OssService.sanitizeSvg(content);
        }
        return new ValidatedImage(content, extension, originalName);
    }

    /**
     * 清理「已上传但最终未保存」的图片（孤儿图）。
     *
     * <p>编辑弹窗里上传的图片会先落到 OSS，若用户中途取消、或反复换图，
     * 这些中间产物从未写进业务表，后端基于「数据库旧值」的清理扫不到，会永久残留。
     * 前端在弹窗关闭时把本次会话上传、但未被最终保存的 URL 送到这里删除。
     *
     * <p>安全约束：{@code deletePublicFileByUrl} 只处理含 {@code haifeng/images/} 或
     * {@code haifeng/institutions/} 等本系统前缀的公共 bucket 对象，外部地址、相对路径、
     * 私有 bucket 文件一律跳过，
     * 即使前端误传也不会删错对象。删除失败只记日志不中断请求。
     */
    @PostMapping("/cleanup-images")
    @OperationLog(module = "通用工具", action = "清理未使用图片")
    public R<Void> cleanupImages(@RequestBody List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return R.ok();
        }
        if (urls.size() > 50) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "单次最多清理 50 张图片");
        }
        int count = 0;
        for (String url : urls) {
            ossService.deletePublicFileByUrl(url);
            count++;
        }
        log.info("清理未使用图片完成: 请求={} 张, 实际删除={} 张", urls.size(), count);
        return R.ok();
    }

    /**
     * 从文件名提取小写扩展名（不含点）。无扩展名返回空串。
     */
    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    /** 校验后的图片载荷 */
    private record ValidatedImage(byte[] content, String extension, String originalName) {
    }
}
