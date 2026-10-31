package com.haifeng.admin.service.impl.certificate;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateBatchStatusDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
import com.haifeng.admin.dto.certificate.CertificateStatusDTO;
import com.haifeng.admin.dto.certificate.CertificateUpdateDTO;
import com.haifeng.admin.excel.certificate.CertificateImportDTO;
import com.haifeng.admin.service.certificate.CertificateService;
import com.haifeng.admin.vo.certificate.CertificateDetailVO;
import com.haifeng.admin.vo.certificate.CertificateListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.common.entity.certificate.Certificate;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CertificateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private static final int MAX_ERROR_DISPLAY = 50;
    private static final int MAX_UPLOAD_NUMBER = 2000;

    private final CertificateMapper certificateMapper;

    @Override
    public IPage<CertificateListVO> listCertificates(CertificateQueryDTO queryDTO) {
        Page<Certificate> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置
        IPage<Certificate> result = certificateMapper.selectPageIgnoreLogicDelete(
                page,
                queryDTO.getIsDeleted(),
                queryDTO.getCertName(),
                queryDTO.getCategory(),
                queryDTO.getCertLevel(),
                queryDTO.getApplicableMajor()
        );

        return result.convert(this::convertToListVO);
    }

    @Override
    public CertificateDetailVO getCertificateDetail(Long id) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置，可以查询已禁用的数据
        Certificate certificate = certificateMapper.findByIdIgnoreLogicDelete(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }
        return convertToDetailVO(certificate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCertificate(CertificateAddDTO addDTO) {
        // 检查证书名称是否重复
        if (certificateMapper.existsByCertName(addDTO.getCertName())) {
            throw new BusinessException(400, "证书名称已存在");
        }

        Certificate certificate = new Certificate();
        BeanUtils.copyProperties(addDTO, certificate);
        certificate.setSortOrder(addDTO.getSortOrder() != null ? addDTO.getSortOrder() : 0);
        certificate.setIsDeleted(false);

        certificateMapper.insert(certificate);
        log.info("新增证书成功，id={}, certName={}", certificate.getId(), certificate.getCertName());
        return certificate.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCertificate(CertificateUpdateDTO updateDTO) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置，可以更新已禁用的数据
        Certificate existing = certificateMapper.findByIdIgnoreLogicDelete(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "证书不存在");
        }

        // 如果修改了名称，检查是否重复
        if (StringUtils.hasText(updateDTO.getCertName())
                && !updateDTO.getCertName().equals(existing.getCertName())) {
            if (certificateMapper.existsByCertName(updateDTO.getCertName())) {
                throw new BusinessException(400, "证书名称已存在");
            }
        }

        // 使用自定义SQL更新，绕过MyBatis-Plus全局逻辑删除配置
        // sortOrder：管理员未传则保留库内原值（existing 已查出，必有值）
        Integer certSortOrder = updateDTO.getSortOrder() != null ? updateDTO.getSortOrder() : existing.getSortOrder();
        certificateMapper.updateByIdIgnoreLogicDelete(
                updateDTO.getId(),
                updateDTO.getCertName(),
                updateDTO.getCategory(),
                updateDTO.getCertLevel(),
                updateDTO.getApplicableMajor(),
                updateDTO.getRegistrationTime(),
                updateDTO.getExamTime(),
                updateDTO.getExamFee(),
                updateDTO.getCertIntro(),
                updateDTO.getExamRequirements(),
                updateDTO.getExamArrangement(),
                updateDTO.getOfficialWebsite(),
                certSortOrder
        );
        log.info("更新证书成功，id={}", updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCertificateStatus(Long id, CertificateStatusDTO dto) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置
        Certificate certificate = certificateMapper.findByIdIgnoreLogicDelete(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        // 使用自定义SQL直接更新is_deleted字段
        certificateMapper.updateIsDeletedById(id, dto.getIsDeleted());
        log.info("修改证书状态成功，id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteCertificate(Long id) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置
        Certificate certificate = certificateMapper.findByIdIgnoreLogicDelete(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        // 使用自定义SQL直接更新is_deleted字段
        certificateMapper.updateIsDeletedById(id, true);
        log.info("软删除证书成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteCertificate(Long id) {
        // 使用自定义SQL绕过MyBatis-Plus全局逻辑删除配置，可以删除已禁用的数据
        Certificate certificate = certificateMapper.findByIdIgnoreLogicDelete(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        // 自定义SQL物理删除（MP 内置 deleteById 会被逻辑删除拦截器转成 UPDATE is_deleted=true）
        certificateMapper.physicalDeleteById(id);
        log.info("硬删除证书成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDeleteCertificates(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 使用自定义SQL进行物理删除，绕过MyBatis-Plus全局逻辑删除配置
        certificateMapper.physicalDeleteBatchByIds(ids);
        log.info("批量硬删除证书成功，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCertificateStatus(CertificateBatchStatusDTO dto) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return;
        }
        // 使用自定义SQL批量更新is_deleted字段，绕过MyBatis-Plus全局逻辑删除配置
        certificateMapper.batchUpdateIsDeletedByIds(dto.getIds(), dto.getIsDeleted());
        log.info("批量修改证书状态成功，ids={}, isDeleted={}", dto.getIds(), dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        // P1: 文件类型校验（仅允许 xlsx / xls）
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new BusinessException(400, "请上传Excel文件（.xlsx或.xls）");
        }

        List<CertificateImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(CertificateImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "Excel文件读取失败，请检查文件格式与单元格数据类型");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        // 归一化字符串字段首尾空格（cert_name 作为唯一键，必须 trim 否则匹配失败）
        for (CertificateImportDTO dto : dataList) {
            if (dto.getCertName() != null) dto.setCertName(dto.getCertName().trim());
            if (dto.getCategory() != null) dto.setCategory(dto.getCategory().trim());
            if (dto.getCertLevel() != null) dto.setCertLevel(dto.getCertLevel().trim());
            if (dto.getApplicableMajor() != null) dto.setApplicableMajor(dto.getApplicableMajor().trim());
            if (dto.getRegistrationTime() != null) dto.setRegistrationTime(dto.getRegistrationTime().trim());
            if (dto.getExamTime() != null) dto.setExamTime(dto.getExamTime().trim());
            if (dto.getCertIntro() != null) dto.setCertIntro(dto.getCertIntro().trim());
            if (dto.getExamRequirements() != null) dto.setExamRequirements(dto.getExamRequirements().trim());
            if (dto.getExamArrangement() != null) dto.setExamArrangement(dto.getExamArrangement().trim());
            if (dto.getOfficialWebsite() != null) dto.setOfficialWebsite(dto.getOfficialWebsite().trim());
        }

        // P2: 行数上限
        if (dataList.size() > MAX_UPLOAD_NUMBER) {
            throw new BusinessException(400, "单次导入不能超过2000条记录");
        }

        // ==================== 校验阶段（只校验，不落库；任一错误整批拒绝） ====================
        List<String> errors = new ArrayList<>();
        Set<String> excelKeys = new HashSet<>();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            CertificateImportDTO dto = dataList.get(i);

            // ---- 必填 + 长度校验（长度对应 DB 列定义） ----
            if (!StringUtils.hasText(dto.getCertName())) {
                errors.add("第" + rowNum + "行: 证书名称不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getCertName().length() > 150) {
                errors.add("第" + rowNum + "行: 证书名称长度不能超过150");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getCategory() != null && dto.getCategory().length() > 50) {
                errors.add("第" + rowNum + "行: 分类长度不能超过50");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getCertLevel() != null && dto.getCertLevel().length() > 50) {
                errors.add("第" + rowNum + "行: 等级长度不能超过50");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getApplicableMajor() != null && dto.getApplicableMajor().length() > 200) {
                errors.add("第" + rowNum + "行: 适用专业长度不能超过200");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getRegistrationTime() != null && dto.getRegistrationTime().length() > 100) {
                errors.add("第" + rowNum + "行: 报名时间长度不能超过100");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getExamTime() != null && dto.getExamTime().length() > 100) {
                errors.add("第" + rowNum + "行: 考试时间长度不能超过100");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getOfficialWebsite() != null && dto.getOfficialWebsite().length() > 500) {
                errors.add("第" + rowNum + "行: 官网链接长度不能超过500");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }

            // ---- 业务范围校验 ----
            // exam_fee：可空；非空则必须 >=0（DB CHECK: exam_fee >= 0）
            if (dto.getExamFee() != null && dto.getExamFee() < 0) {
                errors.add("第" + rowNum + "行: 考试费用不能为负数");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }

            // ---- 表内唯一性校验（cert_name 是业务唯一键） ----
            if (excelKeys.contains(dto.getCertName())) {
                errors.add("第" + rowNum + "行: Excel内存在重复证书名称[" + dto.getCertName() + "]");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            excelKeys.add(dto.getCertName());
        }

        // 校验不过直接整批拒绝（参考实现的潜在缺陷：校验错误被收集却未拦截，此处修正）
        if (!errors.isEmpty()) {
            throw new BusinessException(400, joinErrors(errors));
        }

        // ==================== 导入阶段（补空不覆盖 + 新增；软删除同名→冲突报错；整批回滚） ====================
        List<String> rowErrors = new ArrayList<>();
        int insertCount = 0;
        int updatedCount = 0;   // 已存在记录补空条数
        int rowNum = 1;
        for (CertificateImportDTO dto : dataList) {
            rowNum++;
            String certName = dto.getCertName();
            try {
                Certificate dbRow = certificateMapper.selectByCertNameIgnoreLogicDelete(certName);
                if (dbRow != null && Boolean.TRUE.equals(dbRow.getIsDeleted())) {
                    // 软删除记录：按需求当作冲突报错，不自动复活
                    rowErrors.add("第" + rowNum + "行: 证书名称[" + certName + "]已存在（含已禁用记录），导入冲突");
                    continue;
                }
                if (dbRow != null) {
                    // 启用记录：仅补空不覆盖
                    boolean changed = fillCertificateGaps(dbRow, dto);
                    if (changed) {
                        certificateMapper.updateById(dbRow);
                    }
                    updatedCount++;
                    continue;
                }
                // 库无：新增
                Certificate entity = Certificate.builder()
                        .certName(certName)
                        .category(dto.getCategory())
                        .certLevel(dto.getCertLevel())
                        .applicableMajor(dto.getApplicableMajor())
                        .registrationTime(dto.getRegistrationTime())
                        .examTime(dto.getExamTime())
                        .examFee(dto.getExamFee())
                        .certIntro(dto.getCertIntro())
                        .examRequirements(splitExamRequirements(dto.getExamRequirements()))
                        .examArrangement(dto.getExamArrangement())
                        .officialWebsite(dto.getOfficialWebsite())
                        .sortOrder(0)
                        .isDeleted(false)
                        .build();
                certificateMapper.insert(entity);
                insertCount++;
            } catch (Exception e) {
                rowErrors.add("第" + rowNum + "行: 保存失败[" + certName + "]: " + e.getMessage());
            }
        }

        if (!rowErrors.isEmpty()) {
            throw new BusinessException(400, joinErrors(rowErrors));
        }

        log.info("导入证书数据完成: 新增={}条, 补空={}条", insertCount, updatedCount);
        int total = dataList.size();
        int failed = 0; // 整批回滚，无部分成功
        return ImportResultVO.builder()
                .total(total)
                .success(total - failed)
                .failed(failed)
                .updated(updatedCount)
                .errors(rowErrors)
                .build();
    }

    /**
     * 已存在记录补空不覆盖：仅当 DB 字段为 null/空串(或 exam_requirements 为空列表) 且导入有值时才写入，已有真实数据保留。
     * cert_name 是唯一键，永不覆盖。
     */
    private boolean fillCertificateGaps(Certificate e, CertificateImportDTO dto) {
        boolean changed = false;
        if (!StringUtils.hasText(e.getCategory()) && StringUtils.hasText(dto.getCategory())) {
            e.setCategory(dto.getCategory()); changed = true;
        }
        if (!StringUtils.hasText(e.getCertLevel()) && StringUtils.hasText(dto.getCertLevel())) {
            e.setCertLevel(dto.getCertLevel()); changed = true;
        }
        if (!StringUtils.hasText(e.getApplicableMajor()) && StringUtils.hasText(dto.getApplicableMajor())) {
            e.setApplicableMajor(dto.getApplicableMajor()); changed = true;
        }
        if (!StringUtils.hasText(e.getRegistrationTime()) && StringUtils.hasText(dto.getRegistrationTime())) {
            e.setRegistrationTime(dto.getRegistrationTime()); changed = true;
        }
        if (!StringUtils.hasText(e.getExamTime()) && StringUtils.hasText(dto.getExamTime())) {
            e.setExamTime(dto.getExamTime()); changed = true;
        }
        if (e.getExamFee() == null && dto.getExamFee() != null) {
            e.setExamFee(dto.getExamFee()); changed = true;
        }
        if (!StringUtils.hasText(e.getCertIntro()) && StringUtils.hasText(dto.getCertIntro())) {
            e.setCertIntro(dto.getCertIntro()); changed = true;
        }
        if (isBlankList(e.getExamRequirements())) {
            List<String> reqs = splitExamRequirements(dto.getExamRequirements());
            if (!reqs.isEmpty()) { e.setExamRequirements(reqs); changed = true; }
        }
        if (!StringUtils.hasText(e.getExamArrangement()) && StringUtils.hasText(dto.getExamArrangement())) {
            e.setExamArrangement(dto.getExamArrangement()); changed = true;
        }
        if (!StringUtils.hasText(e.getOfficialWebsite()) && StringUtils.hasText(dto.getOfficialWebsite())) {
            e.setOfficialWebsite(dto.getOfficialWebsite()); changed = true;
        }
        return changed;
    }

    /**
     * 将 Excel 中逗号隔开的考试要求字符串切分为 text[] 元素。
     * 中英文逗号（， / ,）均可；trim 后丢弃空元素；空输入返回空列表。
     */
    private List<String> splitExamRequirements(String raw) {
        List<String> result = new ArrayList<>();
        if (!StringUtils.hasText(raw)) {
            return result;
        }
        String[] parts = raw.split("[，,]");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private boolean isBlankList(List<String> list) {
        return list == null || list.isEmpty();
    }

    private String joinErrors(List<String> errList) {
        if (errList == null || errList.isEmpty()) return null;
        int shown = Math.min(errList.size(), MAX_ERROR_DISPLAY);
        String joined = String.join("; ", errList.subList(0, shown));
        if (errList.size() > MAX_ERROR_DISPLAY) {
            joined += "; ...仅显示前" + MAX_ERROR_DISPLAY + "条，共" + errList.size() + "行错误，详见后端日志";
        }
        return joined;
    }

    private CertificateListVO convertToListVO(Certificate certificate) {
        return CertificateListVO.builder()
                .id(certificate.getId())
                .certName(certificate.getCertName())
                .category(certificate.getCategory())
                .certLevel(certificate.getCertLevel())
                .applicableMajor(certificate.getApplicableMajor())
                .registrationTime(certificate.getRegistrationTime())
                .examTime(certificate.getExamTime())
                .examFee(certificate.getExamFee())
                .isDeleted(certificate.getIsDeleted())
                .sortOrder(certificate.getSortOrder())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .build();
    }

    private CertificateDetailVO convertToDetailVO(Certificate certificate) {
        return CertificateDetailVO.builder()
                .id(certificate.getId())
                .certName(certificate.getCertName())
                .category(certificate.getCategory())
                .certLevel(certificate.getCertLevel())
                .applicableMajor(certificate.getApplicableMajor())
                .registrationTime(certificate.getRegistrationTime())
                .examTime(certificate.getExamTime())
                .examFee(certificate.getExamFee())
                .certIntro(certificate.getCertIntro())
                .examRequirements(certificate.getExamRequirements())
                .examArrangement(certificate.getExamArrangement())
                .officialWebsite(certificate.getOfficialWebsite())
                .sortOrder(certificate.getSortOrder())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .build();
    }
}
