package com.haifeng.admin.service.impl.oss;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.dto.fileload.OssConfirmUploadDTO;
import com.haifeng.admin.dto.fileload.OssPresignUploadDTO;
import com.haifeng.admin.service.oss.OssUploadService;
import com.haifeng.admin.vo.fileload.OssPresignUploadVO;
import com.haifeng.common.config.OssProperties;
import com.haifeng.common.entity.resource.FileInfo;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.resource.FileInfoMapper;
import com.haifeng.common.service.resource.OssService;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssUploadServiceImpl implements OssUploadService {

    private final OssService ossService;
    private final OssProperties ossProperties;
    private final FileInfoMapper fileInfoMapper;

    @Override
    public OssPresignUploadVO presignUpload(OssPresignUploadDTO dto) {
        // 生成预签名上传 URL
        OssService.PresignedUploadResult result = ossService.generatePresignedUploadUrl(dto.getFileName());

        // 获取文件扩展名用于 Content-Type
        String contentType = guessContentType(dto.getFileName());

        log.info("生成预签名上传URL: fileName={}, objectKey={}", dto.getFileName(), result.objectKey());

        return OssPresignUploadVO.builder()
                .uploadUrl(result.uploadUrl())
                .objectKey(result.objectKey())
                .contentType(contentType)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirmUpload(OssConfirmUploadDTO dto) {
        // 检查 objectKey 是否已经确认过（幂等性）
        LambdaQueryWrapper<FileInfo> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FileInfo::getFileUrl, dto.getObjectKey())
                   .eq(FileInfo::getDeleted, false);
        if (fileInfoMapper.selectOne(checkWrapper) != null) {
            throw new BusinessException(409, "该文件已确认上传，请勿重复确认");
        }

        // 提取文件类型
        String fileType = "";
        if (dto.getFileName() != null && dto.getFileName().contains(".")) {
            fileType = dto.getFileName().substring(dto.getFileName().lastIndexOf(".") + 1).toLowerCase();
        }

        // 生成 MD5（如果前端没传）
        String md5 = dto.getFileMd5();
        if (!StringUtils.hasText(md5)) {
            md5 = "oss_direct_" + System.currentTimeMillis();
        }

        Long id = SnowflakeIdGenerator.nextId();
        FileInfo fileInfo = FileInfo.builder()
                .id(id)
                .fileName(dto.getFileName())
                .fileUrl(dto.getObjectKey())
                .fileType(fileType)
                .fileSize(dto.getFileSize())
                .fileMd5(md5)
                .bucketName(ossProperties.getBucketName())
                .targetAudience(dto.getTargetAudience())
                .applicableStage(dto.getApplicableStage())
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .tag(dto.getTag())
                .version(0)
                .createTime(OffsetDateTime.now())
                .updateTime(OffsetDateTime.now())
                .deleted(false)
                .build();

        fileInfoMapper.insert(fileInfo);
        log.info("OSS直传文件确认成功: id={}, fileName={}, objectKey={}", id, dto.getFileName(), dto.getObjectKey());
        return id;
    }

    /**
     * 根据文件扩展名猜测 Content-Type
     */
    private String guessContentType(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "application/octet-stream";
        }
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            case "ppt", "pptx" -> "application/vnd.ms-powerpoint";
            case "txt" -> "text/plain";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}
