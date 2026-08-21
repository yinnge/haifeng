package com.haifeng.app.service.impl.fileload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.fileload.FileLoadService;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileLoadServiceImpl implements FileLoadService {

    private final FileInfoMapper fileInfoMapper;
    private final OssService ossService;
    private final OssProperties ossProperties;
    private final StringRedisTemplate redisTemplate;

    @Override
    public IPage<FileLoadListVO> page(BasePageQueryDTO dto, String targetAudience,
                                       String subject, String applicableStage) {
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

        wrapper.orderByDesc(FileInfo::getCreateTime);

        IPage<FileInfo> fileInfoPage = fileInfoMapper.selectPage(page, wrapper);

        return fileInfoPage.convert(fileInfo -> {
            FileLoadListVO vo = new FileLoadListVO();
            BeanUtils.copyProperties(fileInfo, vo);
            return vo;
        });
    }

    @Override
    public FileLoadDetailVO detail(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }

        FileLoadDetailVO vo = new FileLoadDetailVO();
        BeanUtils.copyProperties(fileInfo, vo);

        // 生成预签名下载URL
        String downloadUrl = ossService.generatePresignedUrl(fileInfo.getFileUrl());
        vo.setDownloadUrl(downloadUrl);

        // 生成KKFileView预览URL
        if (isPreviewable(fileInfo.getFileType())) {
            String encodedUrl = URLEncoder.encode(downloadUrl, StandardCharsets.UTF_8);
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

        // 生成OSS预签名URL（临时可访问）
        String ossUrl = ossService.generatePresignedUrl(fileInfo.getFileUrl());
        String encodedUrl = URLEncoder.encode(ossUrl, StandardCharsets.UTF_8);

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

        return ossService.generatePresignedUrl(fileInfo.getFileUrl());
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
