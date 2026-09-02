package com.haifeng.admin.service.impl.fileload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
                       String applicableStage, String description, String tag, String createBy) {
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

        // 检查是否已存在（按 MD5 去重）
        // 管理端：同一文件（MD5 一致）视为重复上传，明确报错提示，而非静默复用旧记录
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileMd5, md5).eq(FileInfo::getDeleted, false);
        FileInfo existing = fileInfoMapper.selectOne(wrapper);
        if (existing != null) {
            log.warn("上传被拒：已存在相同文件（MD5 一致）: md5={}, existingId={}, fileName={}",
                    md5, existing.getId(), existing.getFileName());
            throw new BusinessException(409, "已存在相同文件（MD5 一致）：" + existing.getFileName() + "，请勿重复上传");
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

        // 预览地址由 detail 接口实时生成 KKFileView Base64 URL（避免存二级域名直链导致 SecondLevelDomainForbidden），这里不落库
        String previewUrl = null;

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
                .description(description)
                .tag(tag)
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
        if (StringUtils.hasText(dto.getTag())) {
            wrapper.eq(FileInfo::getTag, dto.getTag());
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

        // 下载链接（带源文件名 disposition，对齐 user 端；浏览器下载同名自动加 (1)/(2)）
        vo.setFileUrl(ossService.generatePresignedUrl(fileInfo.getFileUrl(), fileInfo.getFileName()));

        // KKFileView 预览链接（实时生成，对齐 user 端）
        // 必须用【干净】的 OSS 预签名 URL（不带 disposition），否则 URL 里的 %+/ 中文编码会让 KKFileView 解析 500。
        // Base64 用标准版（KKFileView 用 Spring Base64Utils 解码，不认 URL-safe 的 -/_）；
        // 标准 Base64 含 +/= 作 query 参数会被当空格/截断，必须再 URLEncoder 一次。
        // KKFileView 内部会 URLDecoder.decode 一次：%2B(+)->+ 被 OSS 当空格->403；%->%25 双重编码抵消。
        if (isPreviewable(fileInfo.getFileType())) {
            String previewOssUrl = ossService.generatePresignedUrl(fileInfo.getFileUrl());
            String b64 = Base64.getEncoder().encodeToString(
                    previewOssUrl.replace("%", "%25").getBytes(StandardCharsets.UTF_8));
            String encodedUrl = URLEncoder.encode(b64, StandardCharsets.UTF_8);
            vo.setFilePreviewUrl(ossProperties.getKkfileviewBaseUrl() + "/onlinePreview?url=" + encodedUrl);
        } else {
            vo.setFilePreviewUrl(null);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, String fileName, String subject, String applicableStage,
                       String description, String tag, Integer version, String updateBy) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }

        // 乐观锁校验
        if (version == null || !version.equals(fileInfo.getVersion())) {
            throw new BusinessException(409, "数据已被其他人修改，请刷新后重试");
        }

        // 用 UpdateWrapper 显式 SET，entity 传 null 绕开 @TableLogic 对 updateById 的拦截干扰
        // （@TableLogic + @Version + updateById 组合会导致逻辑删除/更新静默不生效，见项目已记录坑）
        LambdaUpdateWrapper<FileInfo> uw = new LambdaUpdateWrapper<>();
        uw.eq(FileInfo::getId, id)
          .eq(FileInfo::getVersion, fileInfo.getVersion())
          .set(FileInfo::getSubject, subject)
          .set(FileInfo::getApplicableStage, applicableStage)
          .set(FileInfo::getDescription, description)
          .set(FileInfo::getTag, tag)
          .set(FileInfo::getUpdateBy, updateBy)
          .set(FileInfo::getUpdateTime, OffsetDateTime.now())
          .setSql("version = version + 1");
        int updatedRows = fileInfoMapper.update(null, uw);
        if (updatedRows == 0) {
            // 影响行数为 0：乐观锁 version 不匹配（前端传的 version 与 DB 不一致）或记录已不存在
            throw new BusinessException(409, "修改失败：数据已被其他人修改或不存在，请刷新列表后重试");
        }
        log.info("文件更新成功: id={}, subject={}, applicableStage={}", id, subject, applicableStage);
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
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null || fileInfo.getDeleted()) {
            throw new BusinessException(404, "文件不存在");
        }

        // 删除OSS文件
        ossService.deleteFile(fileInfo.getFileUrl());

        // 同步清理已生成的带水印PDF，避免OSS残留孤儿对象
        if (StringUtils.hasText(fileInfo.getWatermarkedFileUrl())) {
            ossService.deleteFile(fileInfo.getWatermarkedFileUrl());
        }

        // 软删除数据库记录：用 UpdateWrapper 显式 SET deleted=true，entity 传 null 绕开
        // @TableLogic 对 updateById 的拦截干扰（@TableLogic+@Version+updateById 会导致逻辑删除静默不生效）
        LambdaUpdateWrapper<FileInfo> uw = new LambdaUpdateWrapper<>();
        uw.eq(FileInfo::getId, id)
          .eq(FileInfo::getVersion, fileInfo.getVersion())
          .set(FileInfo::getDeleted, true)
          .set(FileInfo::getUpdateTime, OffsetDateTime.now())
          .setSql("version = version + 1");
        int deletedRows = fileInfoMapper.update(null, uw);
        if (deletedRows == 0) {
            // 影响行数为 0：乐观锁 version 不匹配 或 记录已被改/删，或操作到了非预期 schema
            throw new BusinessException(409, "删除失败：记录可能已被修改、不存在，或数据未实际写入（请核对 admin 连接的库与查询客户端是否一致）");
        }

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
