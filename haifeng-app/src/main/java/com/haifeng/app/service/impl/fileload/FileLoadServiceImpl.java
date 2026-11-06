package com.haifeng.app.service.impl.fileload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.fileload.FileLoadService;
import com.haifeng.app.vo.fileload.FileLoadDetailVO;
import com.haifeng.app.vo.fileload.FileLoadListVO;
import com.haifeng.app.vo.fileload.NetdiskInfoVO;
import com.haifeng.app.dto.fileload.FileLoadPageQueryDTO;
import com.haifeng.common.config.OssProperties;
import com.haifeng.common.constant.RedisKeyConstant;
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

    /** 存储方式：百度网盘（不产生 OSS 对象） */
    private static final String STORAGE_TYPE_BAIDU = "BAIDU";

    @Override
    public IPage<FileLoadListVO> page(FileLoadPageQueryDTO dto, String targetAudience,
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

        // 收藏过滤：传入已收藏的文件 id 集合时，仅返回命中项，与上面筛选 AND 叠加。
        // 空集合表示"无收藏" → 直接返回空页，避免 IN () 生成非法 SQL。
        List<Long> ids = dto.getIds();
        if (ids != null && ids.isEmpty()) {
            return new Page<FileLoadListVO>(dto.getPage(), dto.getSize());
        }
        wrapper.in(ids != null, FileInfo::getId, ids);

        // 独立搜索关键词：命中 文件名 或 简介（PostgreSQL ILIKE 不区分大小写包含），
        // 与 subject / applicableStage / tag 三个筛选项以 AND 叠加（先搜后筛、先筛后搜均生效）。
        // 转义 LIKE 通配符 % _，避免用户输入被当作通配符；trim 去除首尾空格。
        String keyword = dto.getKeyword();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim()
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            String pattern = "%" + kw + "%";
            wrapper.and(w -> w
                    .apply("file_name ILIKE {0} ESCAPE '\\'", pattern)
                    .or()
                    .apply("description ILIKE {0} ESCAPE '\\'", pattern));
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

        // 百度网盘资源：无 OSS 对象、无预览/下载，仅回显元信息 + 封面；
        // 网盘链接+提取码不放在详情里（防止登录即绕过 VIP），统一走 /{id}/netdisk（@RequireVip）
        if (STORAGE_TYPE_BAIDU.equals(fileInfo.getStorageType())) {
            vo.setPreviewUrl(null);
            vo.setDownloadUrl(null);
            return vo;
        }

        // 下载直接返回原文件（不再触发水印链路）
        vo.setDownloadUrl(ossService.generatePresignedUrl(fileInfo.getFileUrl(), fileInfo.getFileName()));

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

        if (STORAGE_TYPE_BAIDU.equals(fileInfo.getStorageType())) {
            throw new BusinessException(400, "百度网盘资源不支持在线预览，请通过「获取网盘链接」获取");
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

        if (STORAGE_TYPE_BAIDU.equals(fileInfo.getStorageType())) {
            throw new BusinessException(400, "百度网盘资源请通过「获取网盘链接」获取");
        }

        // 悲观锁：防止用户误触重复下载
        Long userId = SecurityUtil.getCurrentUserId();
        String lockKey = RedisKeyConstant.getFileloadActionLockKey(id, userId, "download");
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", RedisKeyConstant.FILELOAD_ACTION_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            throw new BusinessException(429, "操作频繁，请稍后再试");
        }

        // 下载直接返回原文件（不再触发水印链路）
        return ossService.generatePresignedUrl(fileInfo.getFileUrl(), fileInfo.getFileName());
    }

    @Override
    public NetdiskInfoVO getNetdiskInfo(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }
        if (!STORAGE_TYPE_BAIDU.equals(fileInfo.getStorageType())) {
            throw new BusinessException(400, "该资源不是百度网盘存储");
        }
        if (!StringUtils.hasText(fileInfo.getNetdiskUrl())) {
            throw new BusinessException(400, "该网盘资源链接为空，请联系管理员");
        }

        NetdiskInfoVO vo = new NetdiskInfoVO();
        vo.setNetdiskUrl(fileInfo.getNetdiskUrl());
        vo.setAccessCode(fileInfo.getAccessCode());
        return vo;
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
