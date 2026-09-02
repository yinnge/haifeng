package com.haifeng.app.service.impl.fileload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.fileload.FileLoadService;
import com.haifeng.app.service.watermark.FileWatermarkService;
import com.haifeng.app.vo.fileload.FileLoadDetailVO;
import com.haifeng.app.vo.fileload.FileLoadListVO;
import com.haifeng.common.config.OssProperties;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.resource.FileInfo;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.resource.FileInfoMapper;
import com.haifeng.common.service.resource.OssService;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileLoadServiceImpl implements FileLoadService {

    private final FileInfoMapper fileInfoMapper;
    private final OssService ossService;
    private final OssProperties ossProperties;
    private final StringRedisTemplate redisTemplate;
    private final FileWatermarkService fileWatermarkService;

    @Override
    public IPage<FileLoadListVO> page(BasePageQueryDTO dto, String targetAudience,
                                       String subject, String applicableStage, String tag) {
        Page<FileInfo> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getDeleted, false);
        wrapper.eq(FileInfo::getTargetAudience, targetAudience);

        if (StringUtils.hasText(subject)) {
            wrapper.eq(FileInfo::getSubject, subject);
        }
        if (StringUtils.hasText(applicableStage)) {
            wrapper.eq(FileInfo::getApplicableStage, applicableStage);
        }
        if (StringUtils.hasText(tag)) {
            wrapper.eq(FileInfo::getTag, tag);
        }

        wrapper.orderByDesc(FileInfo::getCreateTime);

        IPage<FileInfo> fileInfoPage = fileInfoMapper.selectPage(page, wrapper);

        return fileInfoPage.convert(fileInfo -> {
            FileLoadListVO vo = new FileLoadListVO();
            BeanUtils.copyProperties(fileInfo, vo);
            return vo;
        });
    }

    @Override
    public List<String> listStages(String targetAudience) {
        return distinctColumn(targetAudience, FileInfo::getApplicableStage);
    }

    @Override
    public List<String> listSubjects(String targetAudience) {
        return distinctColumn(targetAudience, FileInfo::getSubject);
    }

    @Override
    public List<String> listTags(String targetAudience) {
        return distinctColumn(targetAudience, FileInfo::getTag);
    }

    /** 取某受众下指定列的去重非空值（用于前端筛选下拉/按钮） */
    private List<String> distinctColumn(String targetAudience, SFunction<FileInfo, String> column) {
        LambdaQueryWrapper<FileInfo> w = new LambdaQueryWrapper<>();
        w.eq(FileInfo::getDeleted, false)
         .eq(FileInfo::getTargetAudience, targetAudience)
         .isNotNull(column)
         .ne(column, "")
         .groupBy(column)
         .select(column);
        return fileInfoMapper.selectObjs(w).stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public FileLoadDetailVO detail(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }

        FileLoadDetailVO vo = new FileLoadDetailVO();
        BeanUtils.copyProperties(fileInfo, vo);

        // 下载统一走带水印 PDF（首次访问触发生成并缓存到 OSS，后续直接复用）；
        // 不支持水印的格式、转换服务不可用、生成失败时自动降级为原文件
        vo.setDownloadUrl(fileWatermarkService.getWatermarkedDownloadUrl(fileInfo));

        // 生成KKFileView预览URL（4.x 要求 url 参数为 Base64 编码）
        // 【关键1】预览必须用【干净】的 OSS 预签名 URL（不带 disposition），否则 URL 里 response-content-disposition
        // 的 +/中文百分号编码会让 KKFileView 解析时 500（Spring Boot 默认 Whitelabel）。
        // 【关键2】Base64 用【标准版】(getEncoder，A-Za-z0-9+/)：KKFileView 用 Spring Base64Utils 解码，
        // 不认 URL-safe 的 -/_（容器日志实测 Illegal base64 character 5f=_）；标准 Base64 含 +/= 作 query
        // 参数会被当空格/截断，所以【必须再 URL 编码一次】（+→%2B 等）。
        if (isPreviewable(fileInfo.getFileType())) {
            String previewOssUrl = ossService.generatePresignedUrl(fileInfo.getFileUrl());
            // KKFileView 内部会 URLDecoder.decode 一次源 URL：签名 URL 里的 %2B(+) 会被还原成 +，
            // 而 + 在 query 中被 OSS 当作空格 → 签名失效 403（docx 等需下载转换的文件必现，PDF 靠运气）。
            // 解法：% → %25 双重编码，KKFileView 解码一次后恰好还原为原始 %XX，OSS 正常解析。
            String b64 = Base64.getEncoder().encodeToString(
                    previewOssUrl.replace("%", "%25").getBytes(StandardCharsets.UTF_8));
            String encodedUrl = URLEncoder.encode(b64, StandardCharsets.UTF_8);
            vo.setPreviewUrl(ossProperties.getKkfileviewBaseUrl() + "/onlinePreview?url=" + encodedUrl);
        }

        return vo;
    }

    @Override
    public String getPreviewUrl(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }

        if (!isPreviewable(fileInfo.getFileType())) {
            throw new BusinessException(400, "该文件类型不支持预览");
        }

        // 悲观锁：防止用户误触重复预览
        Long userId = SecurityUtil.getCurrentUserId();
        String lockKey = RedisKeyConstant.getFileloadActionLockKey(id, userId, "preview");
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", RedisKeyConstant.FILELOAD_ACTION_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            throw new BusinessException(429, "操作频繁，请稍后再试");
        }

        // 生成【干净】OSS预签名URL（不带 disposition，否则 KKFileView 解析 URL 里的
        // response-content-disposition 中的 +/中文百分号编码时会 500）
        String ossUrl = ossService.generatePresignedUrl(fileInfo.getFileUrl());
        // KKFileView 会 URLDecoder.decode 一次源 URL（%2B→+ 被 OSS 当空格 → 403）；%→%25 双重编码抵消
        String b64 = Base64.getEncoder().encodeToString(
                ossUrl.replace("%", "%25").getBytes(StandardCharsets.UTF_8));
        String encodedUrl = URLEncoder.encode(b64, StandardCharsets.UTF_8);

        // 返回KKFileView预览地址
        return ossProperties.getKkfileviewBaseUrl() + "/onlinePreview?url=" + encodedUrl;
    }

    @Override
    public String getDownloadUrl(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }

        // 悲观锁：防止用户误触重复下载
        Long userId = SecurityUtil.getCurrentUserId();
        String lockKey = RedisKeyConstant.getFileloadActionLockKey(id, userId, "download");
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", RedisKeyConstant.FILELOAD_ACTION_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            throw new BusinessException(429, "操作频繁，请稍后再试");
        }

        // 下载统一输出带水印 PDF（生成结果按文件缓存复用，非每次下载都重算）
        return fileWatermarkService.getWatermarkedDownloadUrl(fileInfo);
    }

    private boolean isPreviewable(String fileType) {
        return "pdf".equals(fileType)
                || "doc".equals(fileType) || "docx".equals(fileType)
                || "xls".equals(fileType) || "xlsx".equals(fileType)
                || "ppt".equals(fileType) || "pptx".equals(fileType)
                || "txt".equals(fileType) || "jpg".equals(fileType)
                || "jpeg".equals(fileType) || "png".equals(fileType)
                || "gif".equals(fileType) || "bmp".equals(fileType);
    }
}
