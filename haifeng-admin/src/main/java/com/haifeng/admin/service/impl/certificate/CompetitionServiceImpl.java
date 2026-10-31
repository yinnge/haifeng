package com.haifeng.admin.service.impl.certificate;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionDetailDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.excel.certificate.CompetitionDetailMainSheetImportDTO;
import com.haifeng.admin.excel.certificate.CompetitionJsonbItemImportDTO;
import com.haifeng.admin.excel.certificate.CompetitionMainImportDTO;
import com.haifeng.admin.service.certificate.CompetitionService;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionDetailMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionMapper competitionMapper;
    private final CompetitionDetailMapper competitionDetailMapper;
    private final CompetitionMajorMapper competitionMajorMapper;

    private static final int MAX_MAIN_ROWS = 1000;        // 主表单次导入行数上限（单 sheet）
    private static final int MAX_SHEET_ROWS = 1000;       // 详情每个 sheet 行数上限
    private static final int MAX_DETAIL_TOTAL = 5000;     // 详情 5 个 sheet 合计行数上限
    private static final int MAX_ERROR_DISPLAY = 50;

    private static final String SHEET_MAIN = "竞赛主表";
    private static final String SHEET_DETAIL_MAIN = "竞赛次表";
    private static final String SHEET_BASIC_INFO = "基本信息";
    private static final String SHEET_RULES = "竞赛规则";
    private static final String SHEET_PROCESS = "参赛流程指南";
    private static final String SHEET_AWARDS_DISPLAY = "奖项设置展示";

    @Override
    public IPage<CompetitionListVO> listCompetitions(CompetitionQueryDTO queryDTO) {
        Page<Competition> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Competition> result = competitionMapper.selectPageIgnoreLogicDelete(
                page, queryDTO.getIsDeleted(), queryDTO.getCompName(), queryDTO.getCompLevel());
        return result.convert(this::convertToListVO);
    }

    @Override
    public CompetitionDetailVO getCompetitionDetail(Long id) {
        Competition competition = competitionMapper.findByIdIgnoreLogicDelete(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);

        return convertToDetailVO(competition, detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCompetition(CompetitionAddDTO addDTO) {
        // 检查竞赛名称是否重复
        if (competitionMapper.existsByCompName(addDTO.getCompName())) {
            throw new BusinessException(400, "竞赛名称已存在");
        }

        // 创建竞赛主表记录
        Competition competition = new Competition();
        BeanUtils.copyProperties(addDTO, competition);
        competition.setSortOrder(addDTO.getSortOrder() != null ? addDTO.getSortOrder() : 0);
        competition.setIsDeleted(false);
        competitionMapper.insert(competition);

        // 创建详情记录
        CompetitionDetail detail = new CompetitionDetail();
        detail.setCompetitionId(competition.getId());
        detail.setIsDeleted(false);

        if (addDTO.getDetail() != null) {
            CompetitionDetailDTO detailDTO = addDTO.getDetail();
            detail.setBasicInfo(detailDTO.getBasicInfo());
            detail.setAwards(detailDTO.getAwards());
            detail.setBackground(detailDTO.getBackground());
            detail.setPurposes(detailDTO.getPurposes());
            detail.setCompetitionRules(detailDTO.getCompetitionRules());
            detail.setScoringCriteria(detailDTO.getScoringCriteria());
            detail.setNotices(detailDTO.getNotices());
            detail.setProcessGuide(detailDTO.getProcessGuide());
            detail.setAwardsDisplay(detailDTO.getAwardsDisplay());
        }

        competitionDetailMapper.insert(detail);

        log.info("新增竞赛成功，id={}, compName={}", competition.getId(), competition.getCompName());
        return competition.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableCompetition(Long id) {
        Competition competition = competitionMapper.findByIdIgnoreLogicDelete(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }
        if (!competition.getIsDeleted()) {
            throw new BusinessException(400, "竞赛已启用");
        }

        competitionMapper.updateIsDeletedById(id, false);

        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null) {
            competitionDetailMapper.updateIsDeletedById(detail.getId(), false);
        }

        // 同步恢复该竞赛的关联专业（启用后用户端可再次看到）
        competitionMajorMapper.enableByCompetitionId(id);

        log.info("启用竞赛成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompetition(CompetitionUpdateDTO updateDTO) {
        Competition existing = competitionMapper.findByIdIgnoreLogicDelete(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        // 如果修改了名称，检查是否重复
        if (StringUtils.hasText(updateDTO.getCompName())
                && !updateDTO.getCompName().equals(existing.getCompName())) {
            if (competitionMapper.existsByCompName(updateDTO.getCompName())) {
                throw new BusinessException(400, "竞赛名称已存在");
            }
        }

        // 更新主表
        existing.setCompName(updateDTO.getCompName());
        existing.setCompLevel(updateDTO.getCompLevel());
        existing.setRegistrationTime(updateDTO.getRegistrationTime());
        if (updateDTO.getSortOrder() != null) {
            existing.setSortOrder(updateDTO.getSortOrder());
        }
        competitionMapper.updateIgnoreLogicDelete(existing);

        // 更新详情表
        if (updateDTO.getDetail() != null) {
            CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(updateDTO.getId());
            if (detail == null) {
                // 历史数据可能没有详情记录（详情表晚于主表上线），此时创建而非静默丢弃
                detail = new CompetitionDetail();
                detail.setCompetitionId(updateDTO.getId());
                detail.setIsDeleted(false);
            }
            CompetitionDetailDTO detailDTO = updateDTO.getDetail();
            detail.setBasicInfo(detailDTO.getBasicInfo());
            detail.setAwards(detailDTO.getAwards());
            detail.setBackground(detailDTO.getBackground());
            detail.setPurposes(detailDTO.getPurposes());
            detail.setCompetitionRules(detailDTO.getCompetitionRules());
            detail.setScoringCriteria(detailDTO.getScoringCriteria());
            detail.setNotices(detailDTO.getNotices());
            detail.setProcessGuide(detailDTO.getProcessGuide());
            detail.setAwardsDisplay(detailDTO.getAwardsDisplay());
            if (detail.getId() != null) {
                competitionDetailMapper.updateIgnoreLogicDelete(detail);
            } else {
                competitionDetailMapper.insert(detail);
            }
        }

        log.info("更新竞赛成功，id={}", updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteCompetition(Long id) {
        Competition competition = competitionMapper.findByIdIgnoreLogicDelete(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }
        if (competition.getIsDeleted()) {
            throw new BusinessException(400, "竞赛已禁用");
        }

        // 禁用主表
        competitionMapper.updateIsDeletedById(id, true);

        // 禁用详情表
        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null) {
            competitionDetailMapper.updateIsDeletedById(detail.getId(), true);
        }

        // 禁用竞赛-专业关联
        competitionMajorMapper.softDeleteByCompetitionId(id);

        log.info("禁用竞赛成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteCompetition(Long id) {
        Competition competition = competitionMapper.findByIdIgnoreLogicDelete(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        // 删除关联的专业记录
        competitionMajorMapper.deleteByCompetitionId(id);

        // 删除详情记录
        competitionDetailMapper.deleteByCompetitionId(id);

        // 物理删除主表记录
        competitionMapper.physicalDeleteById(id);

        log.info("删除竞赛成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDeleteCompetitions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            competitionMajorMapper.deleteByCompetitionId(id);
            competitionDetailMapper.deleteByCompetitionId(id);
        }
        competitionMapper.physicalDeleteBatchByIds(ids);

        log.info("批量删除竞赛成功，ids={}", ids);
    }

    private CompetitionListVO convertToListVO(Competition competition) {
        return CompetitionListVO.builder()
                .id(competition.getId())
                .compName(competition.getCompName())
                .compLevel(competition.getCompLevel())
                .registrationTime(competition.getRegistrationTime())
                .sortOrder(competition.getSortOrder())
                .isDeleted(competition.getIsDeleted())
                .createdAt(competition.getCreatedAt())
                .updatedAt(competition.getUpdatedAt())
                .build();
    }

    private CompetitionDetailVO convertToDetailVO(Competition competition, CompetitionDetail detail) {
        CompetitionDetailVO.CompetitionDetailVOBuilder builder = CompetitionDetailVO.builder()
                .id(competition.getId())
                .compName(competition.getCompName())
                .compLevel(competition.getCompLevel())
                .registrationTime(competition.getRegistrationTime())
                .sortOrder(competition.getSortOrder())
                .createdAt(competition.getCreatedAt())
                .updatedAt(competition.getUpdatedAt());

        if (detail != null) {
            builder.detailId(detail.getId())
                   .basicInfo(detail.getBasicInfo())
                   .awards(detail.getAwards())
                   .background(detail.getBackground())
                   .purposes(detail.getPurposes())
                   .competitionRules(detail.getCompetitionRules())
                   .scoringCriteria(detail.getScoringCriteria())
                   .notices(detail.getNotices())
                   .processGuide(detail.getProcessGuide())
                   .awardsDisplay(detail.getAwardsDisplay());
        }

        return builder.build();
    }

    // ==================== 批量导入：竞赛主表（sheet 竞赛主表） ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importMain(MultipartFile file) {
        checkExcelFile(file);

        List<CompetitionMainImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(CompetitionMainImportDTO.class)
                    .sheet(SHEET_MAIN)
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取竞赛主表Excel失败", e);
            throw new BusinessException(400, "Excel文件读取失败，请检查文件格式与单元格数据类型");
        }
        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }
        if (dataList.size() > MAX_MAIN_ROWS) {
            throw new BusinessException(400, "单次导入不能超过" + MAX_MAIN_ROWS + "条记录");
        }

        // 归一化字符串字段（compName 作为唯一键，必须 trim）
        for (CompetitionMainImportDTO dto : dataList) {
            if (dto.getCompName() != null) dto.setCompName(dto.getCompName().trim());
            if (dto.getCompLevel() != null) dto.setCompLevel(dto.getCompLevel().trim());
            if (dto.getRegistrationTime() != null) dto.setRegistrationTime(dto.getRegistrationTime().trim());
        }

        Map<String, Competition> nameMap = buildNameMap();
        List<String> errors = new ArrayList<>();
        Set<String> excelKeys = new HashSet<>();

        // ==================== 校验阶段（只校验，不落库；任一错误整批拒绝） ====================
        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            CompetitionMainImportDTO dto = dataList.get(i);

            if (!StringUtils.hasText(dto.getCompName())) {
                errors.add("第" + rowNum + "行: 竞赛名称不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getCompName().length() > 200) {
                errors.add("第" + rowNum + "行: 竞赛名称长度不能超过200");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getCompLevel() != null && dto.getCompLevel().length() > 50) {
                errors.add("第" + rowNum + "行: 竞赛级别长度不能超过50");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getRegistrationTime() != null && dto.getRegistrationTime().length() > 100) {
                errors.add("第" + rowNum + "行: 报名时间长度不能超过100");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (excelKeys.contains(dto.getCompName())) {
                errors.add("第" + rowNum + "行: Excel内存在重复竞赛名称[" + dto.getCompName() + "]");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            excelKeys.add(dto.getCompName());
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(400, joinErrors(errors));
        }

        // ==================== 导入阶段（补空不覆盖 + 新增；软删除同名→冲突；整批回滚） ====================
        List<String> rowErrors = new ArrayList<>();
        int insertCount = 0;
        int updatedCount = 0;
        int rowNum = 1;
        for (CompetitionMainImportDTO dto : dataList) {
            rowNum++;
            String compName = dto.getCompName();
            try {
                Competition existing = nameMap.get(compName);
                if (existing != null && Boolean.TRUE.equals(existing.getIsDeleted())) {
                    // 软删除记录：按需求当作冲突报错，不自动复活
                    rowErrors.add("第" + rowNum + "行: 竞赛名称[" + compName + "]已存在（含已禁用记录），导入冲突");
                    continue;
                }
                if (existing != null) {
                    // 启用记录：仅补空不覆盖（compName 唯一键永不覆盖）
                    Competition full = competitionMapper.findByIdIgnoreLogicDelete(existing.getId());
                    boolean changed = fillMainGaps(full, dto);
                    if (changed) {
                        competitionMapper.updateById(full);
                    }
                    updatedCount++;
                    continue;
                }
                // 库无：新增
                Competition entity = Competition.builder()
                        .compName(compName)
                        .compLevel(dto.getCompLevel())
                        .registrationTime(dto.getRegistrationTime())
                        .sortOrder(0)
                        .isDeleted(false)
                        .build();
                competitionMapper.insert(entity);
                insertCount++;
            } catch (Exception e) {
                rowErrors.add("第" + rowNum + "行: 保存失败[" + compName + "]: " + e.getMessage());
            }
        }
        if (!rowErrors.isEmpty()) {
            throw new BusinessException(400, joinErrors(rowErrors));
        }

        int total = dataList.size();
        log.info("导入竞赛主表完成: 新增={}条, 补空={}条", insertCount, updatedCount);
        return ImportResultVO.builder()
                .total(total)
                .success(total)
                .failed(0)
                .updated(updatedCount)
                .errors(rowErrors)
                .build();
    }

    // ==================== 批量导入：竞赛详情（5 个 sheet） ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importDetail(MultipartFile file) {
        checkExcelFile(file);

        // 读取 5 个 sheet（缺 sheet 视为该部分无数据，不报错；EasyExcel 找不到 sheet 会静默返回空列表）
        List<CompetitionDetailMainSheetImportDTO> mainSheet;
        List<CompetitionJsonbItemImportDTO> basicInfoSheet;
        List<CompetitionJsonbItemImportDTO> rulesSheet;
        List<CompetitionJsonbItemImportDTO> processSheet;
        List<CompetitionJsonbItemImportDTO> awardsDisplaySheet;
        try {
            mainSheet = EasyExcel.read(file.getInputStream())
                    .head(CompetitionDetailMainSheetImportDTO.class).sheet(SHEET_DETAIL_MAIN).doReadSync();
            basicInfoSheet = EasyExcel.read(file.getInputStream())
                    .head(CompetitionJsonbItemImportDTO.class).sheet(SHEET_BASIC_INFO).doReadSync();
            rulesSheet = EasyExcel.read(file.getInputStream())
                    .head(CompetitionJsonbItemImportDTO.class).sheet(SHEET_RULES).doReadSync();
            processSheet = EasyExcel.read(file.getInputStream())
                    .head(CompetitionJsonbItemImportDTO.class).sheet(SHEET_PROCESS).doReadSync();
            awardsDisplaySheet = EasyExcel.read(file.getInputStream())
                    .head(CompetitionJsonbItemImportDTO.class).sheet(SHEET_AWARDS_DISPLAY).doReadSync();
        } catch (Exception e) {
            log.error("读取竞赛详情Excel失败", e);
            throw new BusinessException(400, "Excel文件读取失败，请检查文件格式与单元格数据类型");
        }

        // 每 sheet 行数上限
        checkSheetSize(SHEET_DETAIL_MAIN, mainSheet);
        checkSheetSize(SHEET_BASIC_INFO, basicInfoSheet);
        checkSheetSize(SHEET_RULES, rulesSheet);
        checkSheetSize(SHEET_PROCESS, processSheet);
        checkSheetSize(SHEET_AWARDS_DISPLAY, awardsDisplaySheet);

        int totalRows = safeSize(mainSheet) + safeSize(basicInfoSheet) + safeSize(rulesSheet)
                + safeSize(processSheet) + safeSize(awardsDisplaySheet);
        if (totalRows == 0) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }
        if (totalRows > MAX_DETAIL_TOTAL) {
            throw new BusinessException(400, "单次导入详情不能超过" + MAX_DETAIL_TOTAL + "行");
        }

        Map<String, Competition> nameMap = buildNameMap();
        List<String> errors = new ArrayList<>();
        Map<String, DetailProposal> byName = new HashMap<>();
        Set<String> mainSheetNames = new HashSet<>();

        // ==================== 校验阶段（全量校验，任一错误整批回滚） ====================

        // ---- 竞赛次表：标量字段（奖项/背景/目的/评分/注意），一张竞赛一行 ----
        if (mainSheet != null) {
            for (int i = 0; i < mainSheet.size(); i++) {
                int rowNum = i + 2;
                CompetitionDetailMainSheetImportDTO dto = mainSheet.get(i);
                String compName = normalize(dto.getCompName());
                if (!StringUtils.hasText(compName)) {
                    addErr(errors, SHEET_DETAIL_MAIN, rowNum, "竞赛名称不能为空");
                    if (errors.size() >= MAX_ERROR_DISPLAY) break;
                    continue;
                }
                if (compName.length() > 200) {
                    addErr(errors, SHEET_DETAIL_MAIN, rowNum, "竞赛名称长度不能超过200");
                    if (errors.size() >= MAX_ERROR_DISPLAY) break;
                    continue;
                }
                if (!nameMap.containsKey(compName)) {
                    addErr(errors, SHEET_DETAIL_MAIN, rowNum, "竞赛名称[" + compName + "]不存在");
                    if (errors.size() >= MAX_ERROR_DISPLAY) break;
                    continue;
                }
                if (mainSheetNames.contains(compName)) {
                    addErr(errors, SHEET_DETAIL_MAIN, rowNum, "竞赛次表内存在重复竞赛名称[" + compName + "]");
                    if (errors.size() >= MAX_ERROR_DISPLAY) break;
                    continue;
                }
                mainSheetNames.add(compName);
                DetailProposal p = byName.computeIfAbsent(compName, k -> new DetailProposal());
                p.awards = dto.getAwards();
                p.background = dto.getBackground();
                p.purposes = dto.getPurposes();
                p.scoringCriteria = dto.getScoringCriteria();
                p.notices = dto.getNotices();
            }
        }

        // ---- 4 个 JSONB sheet：每条 = {title, content} ----
        if (errors.size() < MAX_ERROR_DISPLAY) {
            collectJsonb(errors, nameMap, byName, basicInfoSheet, SHEET_BASIC_INFO, (p, item) -> {
                if (p.basicInfo == null) p.basicInfo = new ArrayList<>();
                p.basicInfo.add(item);
            });
        }
        if (errors.size() < MAX_ERROR_DISPLAY) {
            collectJsonb(errors, nameMap, byName, rulesSheet, SHEET_RULES, (p, item) -> {
                if (p.competitionRules == null) p.competitionRules = new ArrayList<>();
                p.competitionRules.add(item);
            });
        }
        if (errors.size() < MAX_ERROR_DISPLAY) {
            collectJsonb(errors, nameMap, byName, processSheet, SHEET_PROCESS, (p, item) -> {
                if (p.processGuide == null) p.processGuide = new ArrayList<>();
                p.processGuide.add(item);
            });
        }
        if (errors.size() < MAX_ERROR_DISPLAY) {
            collectJsonb(errors, nameMap, byName, awardsDisplaySheet, SHEET_AWARDS_DISPLAY, (p, item) -> {
                if (p.awardsDisplay == null) p.awardsDisplay = new ArrayList<>();
                p.awardsDisplay.add(item);
            });
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(400, joinErrors(errors));
        }

        // ==================== 导入阶段（补空不覆盖；整批事务，出错回滚） ====================
        int insertCount = 0;
        int updatedCount = 0;
        for (Map.Entry<String, DetailProposal> entry : byName.entrySet()) {
            String compName = entry.getKey();
            DetailProposal p = entry.getValue();
            Competition comp = nameMap.get(compName);
            Long compId = comp.getId();
            try {
                CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(compId);
                boolean isNew = (detail == null);
                if (isNew) {
                    detail = new CompetitionDetail();
                    detail.setCompetitionId(compId);
                    // 镜像主表的删除状态：若主表已软删除，则详情也建为软删除，避免给禁用竞赛创建启用态详情
                    detail.setIsDeleted(comp.getIsDeleted());
                }
                boolean changed = fillDetailGaps(detail, p);
                if (isNew) {
                    if (changed) {
                        competitionDetailMapper.insert(detail);
                        insertCount++;
                    }
                } else {
                    if (changed) {
                        competitionDetailMapper.updateIgnoreLogicDelete(detail);
                        updatedCount++;
                    }
                }
            } catch (Exception e) {
                throw new BusinessException(400, "竞赛名称[" + compName + "]详情保存失败: " + e.getMessage());
            }
        }

        int total = byName.size();
        int success = insertCount + updatedCount;
        log.info("导入竞赛详情完成: 竞赛数={}, 新增详情={}条, 补空详情={}条", total, insertCount, updatedCount);
        return ImportResultVO.builder()
                .total(total)
                .success(success)
                .failed(0)
                .updated(updatedCount)
                .errors(new ArrayList<>())
                .build();
    }

    // ==================== 导入辅助方法 ====================

    private void checkExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new BusinessException(400, "请上传Excel文件（.xlsx或.xls）");
        }
    }

    private void checkSheetSize(String sheetName, List<?> list) {
        if (list != null && list.size() > MAX_SHEET_ROWS) {
            throw new BusinessException(400, "导入失败：" + sheetName + "Sheet数据不能超过" + MAX_SHEET_ROWS + "行");
        }
    }

    private int safeSize(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private Map<String, Competition> buildNameMap() {
        List<Competition> all = competitionMapper.selectAllIdName();
        Map<String, Competition> map = new HashMap<>();
        if (all != null) {
            for (Competition c : all) {
                if (c.getCompName() != null) {
                    map.put(c.getCompName(), c);
                }
            }
        }
        return map;
    }

    private boolean fillMainGaps(Competition e, CompetitionMainImportDTO dto) {
        boolean changed = false;
        if (!StringUtils.hasText(e.getCompLevel()) && StringUtils.hasText(dto.getCompLevel())) {
            e.setCompLevel(dto.getCompLevel());
            changed = true;
        }
        if (!StringUtils.hasText(e.getRegistrationTime()) && StringUtils.hasText(dto.getRegistrationTime())) {
            e.setRegistrationTime(dto.getRegistrationTime());
            changed = true;
        }
        return changed;
    }

    private boolean fillDetailGaps(CompetitionDetail e, DetailProposal p) {
        boolean changed = false;
        if (isBlankList(e.getBasicInfo()) && hasItems(p.basicInfo)) {
            e.setBasicInfo(p.basicInfo); changed = true;
        }
        if (isBlankList(e.getAwards()) && hasItems(p.awards)) {
            e.setAwards(p.awards); changed = true;
        }
        if (!StringUtils.hasText(e.getBackground()) && StringUtils.hasText(p.background)) {
            e.setBackground(p.background); changed = true;
        }
        if (isBlankList(e.getPurposes()) && hasItems(p.purposes)) {
            e.setPurposes(p.purposes); changed = true;
        }
        if (isBlankList(e.getCompetitionRules()) && hasItems(p.competitionRules)) {
            e.setCompetitionRules(p.competitionRules); changed = true;
        }
        if (isBlankList(e.getScoringCriteria()) && hasItems(p.scoringCriteria)) {
            e.setScoringCriteria(p.scoringCriteria); changed = true;
        }
        if (isBlankList(e.getNotices()) && hasItems(p.notices)) {
            e.setNotices(p.notices); changed = true;
        }
        if (isBlankList(e.getProcessGuide()) && hasItems(p.processGuide)) {
            e.setProcessGuide(p.processGuide); changed = true;
        }
        if (isBlankList(e.getAwardsDisplay()) && hasItems(p.awardsDisplay)) {
            e.setAwardsDisplay(p.awardsDisplay); changed = true;
        }
        return changed;
    }

    /**
     * 读取单个 JSONB sheet（基本信息/竞赛规则/参赛流程指南/奖项设置展示），校验竞赛名称存在性与标题必填，
     * 通过 appender 把 {title, content} 元素追加到对应竞赛的 DetailProposal 列表中。
     */
    private void collectJsonb(List<String> errors, Map<String, Competition> nameMap,
                              Map<String, DetailProposal> byName,
                              List<CompetitionJsonbItemImportDTO> rows, String sheetName,
                              BiConsumer<DetailProposal, Map<String, String>> appender) {
        if (rows == null) return;
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            CompetitionJsonbItemImportDTO dto = rows.get(i);
            String compName = normalize(dto.getCompName());
            if (!StringUtils.hasText(compName)) {
                addErr(errors, sheetName, rowNum, "竞赛名称不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) return;
                continue;
            }
            if (!nameMap.containsKey(compName)) {
                addErr(errors, sheetName, rowNum, "竞赛名称[" + compName + "]不存在");
                if (errors.size() >= MAX_ERROR_DISPLAY) return;
                continue;
            }
            if (!StringUtils.hasText(dto.getTitle())) {
                addErr(errors, sheetName, rowNum, "标题不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) return;
                continue;
            }
            DetailProposal p = byName.computeIfAbsent(compName, k -> new DetailProposal());
            Map<String, String> item = new HashMap<>();
            item.put("title", dto.getTitle().trim());
            item.put("content", StringUtils.hasText(dto.getContent()) ? dto.getContent().trim() : "");
            appender.accept(p, item);
        }
    }

    private boolean hasItems(List<?> list) {
        return list != null && !list.isEmpty();
    }

    private boolean isBlankList(List<?> list) {
        return list == null || list.isEmpty();
    }

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    private void addErr(List<String> errors, String sheetName, int rowNum, String msg) {
        errors.add("第[" + sheetName + "]第" + rowNum + "行: " + msg);
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

    @Data
    private static class DetailProposal {
        private List<String> awards;
        private String background;
        private List<String> purposes;
        private List<String> scoringCriteria;
        private List<String> notices;
        private List<Map<String, String>> basicInfo;
        private List<Map<String, String>> competitionRules;
        private List<Map<String, String>> processGuide;
        private List<Map<String, String>> awardsDisplay;
    }
}
