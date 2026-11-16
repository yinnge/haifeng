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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
     * 上传公开素材图片
     *
     * <p>图片写入独立的「公共读」bucket，返回永久有效的完整 URL，
     * 可直接被 C 端 {@code <img src>} 引用（头像 / 轮播图 / 微信二维码等）。
     * 私有 bucket 承载 VIP 门禁文件，绝不复用。
     */
    @PostMapping("/upload")
    @OperationLog(module = "通用工具", action = "上传图片")
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

        // SVG 可内嵌脚本，上传到公共读 bucket 等于对外发布可执行内容，必须先消毒
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片读取失败");
        }
        if ("svg".equals(extension)) {
            content = sanitizeSvg(content);
        }

        String url = ossService.uploadPublicBytes(content, "." + extension);
        log.info("上传图片成功: url={}, size={}", url, content.length);
        return R.ok(new ImageUploadVO(url, originalName, (long) content.length));
    }

    /**
     * SVG 消毒：剥离 {@code <script>} 块、事件属性（on*=）与 javascript: 伪协议。
     * <p>公共读 bucket 上的 SVG 可被任意人直接访问，内嵌脚本会形成存储型 XSS。
     *
     * @return 消毒后的字节内容
     */
    private byte[] sanitizeSvg(byte[] raw) {
        String content = new String(raw, StandardCharsets.UTF_8);

        String cleaned = content
                // 去掉 <script ...>...</script> 整块（含大小写混写）
                .replaceAll("(?is)<script[^>]*>.*?</script>", "")
                // 去掉自闭合/独立的 <script ... />
                .replaceAll("(?is)<script[^>]*/?>", "")
                // 去掉 on*= 事件属性（onload= / onclick= / onerror= 等）
                .replaceAll("(?is)\\son[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", "")
                // 去掉 javascript: 伪协议
                .replaceAll("(?is)javascript\\s*:", "");

        if (cleaned.length() != content.length()) {
            log.warn("上传的 SVG 含潜在脚本内容，已做剥离处理");
            return cleaned.getBytes(StandardCharsets.UTF_8);
        }
        return raw;
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
}
