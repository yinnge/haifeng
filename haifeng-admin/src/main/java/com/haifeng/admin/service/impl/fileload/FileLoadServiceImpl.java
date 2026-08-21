package com.haifeng.admin.service.impl.fileload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.fileload.FileLoadQueryDTO;
import com.haifeng.admin.service.fileload.FileLoadService;
import com.haifeng.admin.vo.fileload.FileLoadDetailVO;
import com.haifeng.admin.vo.fileload.FileLoadListVO;
import com.haifeng.common.config.OssProperties;
import com.haifeng.common.entity.resource.FileInfo;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.resource.FileInfoMapper;
import com.haifeng.common.service.resource.OssService;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileLoadServiceImpl implements FileLoadService {

    private final FileInfoMapper fileInfoMapper;
    private final OssService ossService;
    private final OssProperties ossProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upload(MultipartFile file, String targetAudience, String subject,
                       String applicableStage, String createBy) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(400, "文件名不能为空");
        }

        // 计算MD5
        String md5;
        try (InputStream is = file.getInputStream()) {
            md5 = ossService.calculateMd5(is);
        } catch (Exception e) {
            throw new BusinessException(400, "计算文件MD5失败");
        }

        // 检查是否已存在（秒传）
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileMd5, md5).eq(FileInfo::getDeleted, false);
        FileInfo existing = fileInfoMapper.selectOne(wrapper);
        if (existing != null) {
            log.info("文件已存在（秒传）: md5={}, id={}", md5, existing.getId());
            return existing.getId();
        }

        // 上传到OSS
        String objectKey = ossService.uploadFile(file);

        // 生成预签名URL
        String presignedUrl = ossService.generatePresignedUrl(objectKey);

        // 提取文件类型
        String fileType = "";
        if (originalFilename.contains(".")) {
            fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        // 构建KKFileView预览地址
        String previewUrl = null;
        if (isPreviewable(fileType)) {
            previewUrl = ossProperties.getEndpoint() + "/" + ossProperties.getBucketName() + "/" + objectKey;
        }

        Long id = SnowflakeIdGenerator.nextId();
        FileInfo fileInfo = FileInfo.builder()
                .id(id)
                .fileName(originalFilename)
                .fileUrl(objectKey)
                .filePreviewUrl(previewUrl)
                .fileType(fileType)
                .fileSize(file.getSize())
                .fileMd5(md5)
                .bucketName(ossProperties.getBucketName())
                .targetAudience(targetAudience)
                .applicableStage(applicableStage)
                .subject(subject)
                .version(0)
                .createBy(createBy)
                .createTime(OffsetDateTime.now())
                .updateBy(createBy)
                .updateTime(OffsetDateTime.now())
                .deleted(false)
                .build();

        fileInfoMapper.insert(fileInfo);
        log.info("文件上传成功: id={}, fileName={}, targetAudience={}, subject={}", id, originalFilename, targetAudience, subject);
        return id;
    }

    @Override
    public IPage<FileLoadListVO> page(FileLoadQueryDTO dto, String targetAudience) {
        Page<FileInfo> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getDeleted, false);
        wrapper.eq(FileInfo::getTargetAudience, targetAudience);

        if (StringUtils.hasText(dto.getFileName())) {
            wrapper.like(FileInfo::getFileName, dto.getFileName());
        }
        if (StringUtils.hasText(dto.getSubject())) {
            wrapper.eq(FileInfo::getSubject, dto.getSubject());
        }
        if (StringUtils.hasText(dto.getApplicableStage())) {
            wrapper.eq(FileInfo::getApplicableStage, dto.getApplicableStage());
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

        // 生成最新的预签名下载URL
        vo.setFileUrl(ossService.generatePresignedUrl(fileInfo.getFileUrl()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, String fileName, String subject, String applicableStage,
                       Integer version, String updateBy) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }

        // 乐观锁校验
        if (version == null || !version.equals(fileInfo.getVersion())) {
            throw new BusinessException(409, "数据已被其他人修改，请刷新后重试");
        }

        fileInfo.setSubject(subject);
        fileInfo.setApplicableStage(applicableStage);
        fileInfo.setVersion(fileInfo.getVersion() + 1);
        fileInfo.setUpdateBy(updateBy);
        fileInfo.setUpdateTime(OffsetDateTime.now());

        fileInfoMapper.updateById(fileInfo);
        log.info("文件更新成功: id={}, subject={}, applicableStage={}", id, subject, applicableStage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }

        // 删除OSS文件
        ossService.deleteFile(fileInfo.getFileUrl());

        // 软删除数据库记录
        fileInfo.setDeleted(true);
        fileInfo.setUpdateTime(OffsetDateTime.now());
        fileInfoMapper.updateById(fileInfo);

        log.info("文件删除成功: id={}, fileName={}", id, fileInfo.getFileName());
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
