package com.haifeng.app.util.algorithm.wish;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.haifeng.common.entity.algorithm.wish.WishGroupSnapshot;
import com.haifeng.common.entity.algorithm.wish.WishMajorSnapshot;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 志愿方案Excel导出工具类
 */
@Slf4j
@Component
public class WishPlanExcelUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出志愿方案到Excel
     *
     * @param outputStream 输出流
     * @param wishPlan     志愿方案
     * @param groups       专业组列表（已按groupSortOrder排序）
     * @param majorsMap    专业组ID -> 专业列表（已按majorSortOrder排序）
     * @param exportMajors 导出的专业ID集合（isExported=true）
     */
    public void exportToExcel(OutputStream outputStream,
                              WishPlan wishPlan,
                              List<WishGroupSnapshot> groups,
                              Map<Integer, List<WishMajorSnapshot>> majorsMap,
                              Set<Integer> exportMajors) {
        try {
            ExcelWriter excelWriter = EasyExcel.write(outputStream)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .registerWriteHandler(new WishPlanCellWriteHandler())
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet("志愿方案").build();

            List<List<Object>> dataList = buildDataList(wishPlan, groups, majorsMap, exportMajors);
            excelWriter.write(dataList, writeSheet);

            excelWriter.finish();
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new BusinessException(ResultCode.EXPORT_FAILED);
        }
    }

    private List<List<Object>> buildDataList(WishPlan wishPlan,
                                             List<WishGroupSnapshot> groups,
                                             Map<Integer, List<WishMajorSnapshot>> majorsMap,
                                             Set<Integer> exportMajors) {
        List<List<Object>> dataList = new ArrayList<>();

        List<Object> firstRow = new ArrayList<>();
        String firstRowContent = String.format("【%s】【%s】【%s】【%s】【%s】 %d分/%d名 %s",
                wishPlan.getPlanName(),
                wishPlan.getPlanYear(),
                wishPlan.getPlanProvince(),
                wishPlan.getPlanBatch(),
                wishPlan.getReformModel(),
                wishPlan.getUserScore(),
                wishPlan.getUserRank(),
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
        firstRow.add(firstRowContent);
        dataList.add(firstRow);

        List<Object> headerRow = new ArrayList<>();
        headerRow.add("组号");
        headerRow.add("大学信息");
        headerRow.add("院校组代码");
        headerRow.add("院校组名称");
        headerRow.add("描述");
        headerRow.add("专业数量");
        headerRow.add("推免年份");
        headerRow.add("推免率");
        headerRow.add("序号");
        headerRow.add("专业名称");
        headerRow.add("学费/学制");
        for (int i = 1; i <= 5; i++) {
            headerRow.add("年份" + i);
        }
        for (int i = 1; i <= 5; i++) {
            headerRow.add("计划招生人数" + i);
        }
        for (int i = 1; i <= 5; i++) {
            headerRow.add("最低分" + i);
        }
        for (int i = 1; i <= 5; i++) {
            headerRow.add("最低位次" + i);
        }
        for (int i = 1; i <= 5; i++) {
            headerRow.add("平均分" + i);
        }
        for (int i = 1; i <= 5; i++) {
            headerRow.add("平均位次" + i);
        }
        for (int i = 1; i <= 5; i++) {
            headerRow.add("最高分" + i);
        }
        for (int i = 1; i <= 5; i++) {
            headerRow.add("最高位次" + i);
        }
        dataList.add(headerRow);

        for (WishGroupSnapshot group : groups) {
            List<WishMajorSnapshot> majors = majorsMap.getOrDefault(group.getId(), Collections.emptyList());
            List<WishMajorSnapshot> filteredMajors = majors.stream()
                    .filter(m -> exportMajors.contains(m.getId()))
                    .collect(Collectors.toList());

            if (filteredMajors.isEmpty()) {
                continue;
            }

            for (int i = 0; i < filteredMajors.size(); i++) {
                WishMajorSnapshot major = filteredMajors.get(i);
                List<Object> row = new ArrayList<>();

                if (i == 0) {
                    row.add(group.getGroupSortOrder());
                    row.add(buildUniversityInfo(group));
                    row.add(group.getGroupCode());
                    row.add(buildGroupNameInfo(group));
                    row.add(buildDescriptionInfo(group));
                    row.add(group.getMajorCount());
                    row.add(group.getRecommendationYear());
                    row.add(group.getRecommendationRate());
                } else {
                    for (int j = 0; j < 8; j++) {
                        row.add("");
                    }
                }

                row.add(major.getMajorSortOrder());
                row.add(buildMajorNameInfo(major));
                row.add(formatDurationTuition(major));

                List<WishMajorSnapshot.HistoryScore> historyScores = major.getHistoryScores();
                if (historyScores != null && !historyScores.isEmpty()) {
                    List<WishMajorSnapshot.HistoryScore> sortedScores = historyScores.stream()
                            .sorted(Comparator.comparing(WishMajorSnapshot.HistoryScore::getYear).reversed())
                            .collect(Collectors.toList());

                    addColumnValues(row, sortedScores, 5, s -> s.getYear() != null ? s.getYear().toString() : "");
                    addColumnValues(row, sortedScores, 5, s -> s.getAdmissionCount() != null ? s.getAdmissionCount().toString() : "");
                    addColumnValues(row, sortedScores, 5, s -> s.getMinScore() != null ? s.getMinScore().toString() : "");
                    addColumnValues(row, sortedScores, 5, s -> s.getMinRank() != null ? s.getMinRank().toString() : "");
                    addColumnValues(row, sortedScores, 5, s -> formatBigDecimal(s.getAvgScore()));
                    addColumnValues(row, sortedScores, 5, s -> s.getAvgRank() != null ? s.getAvgRank().toString() : "");
                    addColumnValues(row, sortedScores, 5, s -> s.getMaxScore() != null ? s.getMaxScore().toString() : "");
                    addColumnValues(row, sortedScores, 5, s -> s.getMaxRank() != null ? s.getMaxRank().toString() : "");
                } else {
                    for (int j = 0; j < 40; j++) {
                        row.add("");
                    }
                }

                dataList.add(row);
            }
        }

        return dataList;
    }

    private <T> void addColumnValues(List<Object> row,
                                     List<T> items,
                                     int maxColumns,
                                     java.util.function.Function<T, String> extractor) {
        for (int j = 0; j < maxColumns; j++) {
            if (j < items.size()) {
                row.add(extractor.apply(items.get(j)));
            } else {
                row.add("");
            }
        }
    }

    private String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private String formatDurationTuition(WishMajorSnapshot major) {
        String duration = major.getDuration() != null ? major.getDuration() : "";
        String tuitionStr = major.getTuition() != null ? major.getTuition() : "";
        if (!duration.isEmpty() && !tuitionStr.isEmpty()) {
            return duration + "/" + tuitionStr;
        } else if (!duration.isEmpty()) {
            return duration;
        } else if (!tuitionStr.isEmpty()) {
            return tuitionStr;
        }
        return "";
    }

    private String buildUniversityInfo(WishGroupSnapshot group) {
        StringBuilder sb = new StringBuilder();
        sb.append(group.getUniversityName());
        sb.append(" ").append(group.getCityName());
        if (group.getCategory() != null) {
            sb.append(" ").append(group.getCategory());
        }
        if (group.getNature() != null) {
            sb.append(" ").append(group.getNature());
        }
        if (group.getTags() != null && !group.getTags().isEmpty()) {
            sb.append(" ").append(String.join(",", group.getTags()));
        }
        return sb.toString();
    }

    private String buildGroupNameInfo(WishGroupSnapshot group) {
        StringBuilder sb = new StringBuilder();
        sb.append(group.getGroupName());
        if (group.getEnrollmentCode() != null) {
            sb.append(" ").append(group.getEnrollmentCode());
        }
        if (group.getSubjects() != null && !group.getSubjects().isEmpty()) {
            sb.append(" ").append(String.join(",", group.getSubjects()));
        }
        return sb.toString();
    }

    private String buildDescriptionInfo(WishGroupSnapshot group) {
        StringBuilder sb = new StringBuilder();
        if (group.getDescription() != null) {
            sb.append(group.getDescription());
        }
        if (group.getConstraintsDescription() != null && !group.getConstraintsDescription().isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(String.join("\n", group.getConstraintsDescription()));
        }
        return sb.toString();
    }

    private String buildMajorNameInfo(WishMajorSnapshot major) {
        StringBuilder sb = new StringBuilder();
        sb.append(major.getMajorName());
        sb.append(" ").append(major.getMajorCode());
        if (major.getDescription() != null) {
            sb.append("\n").append(major.getDescription());
        }
        return sb.toString();
    }

    /**
     * 自定义CellWriteHandler用于合并第一行单元格
     */
    private static class WishPlanCellWriteHandler implements CellWriteHandler {

        @Override
        public void afterCellDispose(CellWriteHandlerContext context) {
            Sheet sheet = context.getWriteSheetHolder().getSheet();
            org.apache.poi.ss.usermodel.Cell cell = context.getCell();

            if (cell == null || cell.getRowIndex() != 0) {
                return;
            }

            int lastCol = sheet.getRow(0).getLastCellNum();
            if (lastCol > 1) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol - 1));
            }
        }
    }
}
