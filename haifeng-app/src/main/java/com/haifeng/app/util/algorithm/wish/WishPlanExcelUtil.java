package com.haifeng.app.util.algorithm.wish;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.haifeng.common.entity.algorithm.wish.WishGroupSnapshot;
import com.haifeng.common.entity.algorithm.wish.WishMajorSnapshot;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 志愿方案Excel导出工具类
 */
@Component
@Slf4j
public class WishPlanExcelUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出志愿方案到Excel
     *
     * @param outputStream 输出流
     * @param wishPlan     志愿方案
     * @param groups       专业组列表（已按groupSortOrder排序）
     * @param majorsMap    专业组ID -> 专业列表（已按majorSortOrder排序）
     * @param exportMajors 导出的专业ID集合（null表示回退到数据库 is_exported 字段）
     */
    public void exportToExcel(OutputStream outputStream,
                              WishPlan wishPlan,
                              List<WishGroupSnapshot> groups,
                              Map<Integer, List<WishMajorSnapshot>> majorsMap,
                              Set<Integer> exportMajors) {
        try {
            List<CellRangeAddress> mergeRegions = new ArrayList<>();
            WishPlanStyleCellHandler styleCellHandler = new WishPlanStyleCellHandler(mergeRegions);

            ExcelWriter excelWriter = EasyExcel.write(outputStream)
                    .registerWriteHandler(styleCellHandler)
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet("志愿方案").build();

            List<List<Object>> dataList = buildDataList(wishPlan, groups, majorsMap, exportMajors, mergeRegions);
            excelWriter.write(dataList, writeSheet);
            styleCellHandler.applyMerges();
            excelWriter.finish();
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new BusinessException(ResultCode.EXPORT_FAILED);
        }
    }

    private List<List<Object>> buildDataList(WishPlan wishPlan,
                                             List<WishGroupSnapshot> groups,
                                             Map<Integer, List<WishMajorSnapshot>> majorsMap,
                                             Set<Integer> exportMajors,
                                             List<CellRangeAddress> mergeRegions) {
        List<List<Object>> dataList = new ArrayList<>();

        // 行0：标题行（19个元素，1个内容+18空串），合并(0,0,0,18)
        List<Object> titleRow = new ArrayList<>();
        String titleContent = String.format("【%s】【%s】【%s】【%s】【%s】 %d分/%d名 %s",
                wishPlan.getPlanName(),
                wishPlan.getPlanYear(),
                wishPlan.getPlanProvince(),
                wishPlan.getPlanBatch(),
                wishPlan.getReformModel(),
                wishPlan.getUserScore(),
                wishPlan.getUserRank(),
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
        titleRow.add(titleContent);
        for (int i = 0; i < 18; i++) {
            titleRow.add("");
        }
        dataList.add(titleRow);
        mergeRegions.add(new CellRangeAddress(0, 0, 0, 18));

        // 行1：表头行（19列表头）
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
        headerRow.add("年份");
        headerRow.add("计划招生人数");
        headerRow.add("最低分");
        headerRow.add("最低位次");
        headerRow.add("平均分");
        headerRow.add("平均位次");
        headerRow.add("最高分");
        headerRow.add("最高位次");
        dataList.add(headerRow);

        // 行2+：数据行，每个专业按 historyScores 最多5行展开
        int currentRow = 2;
        for (WishGroupSnapshot group : groups) {
            List<WishMajorSnapshot> majors = majorsMap.getOrDefault(group.getId(), Collections.emptyList());
            List<WishMajorSnapshot> filteredMajors = filterMajors(majors, exportMajors);

            if (filteredMajors.isEmpty()) {
                continue;
            }

            for (WishMajorSnapshot major : filteredMajors) {
                List<WishMajorSnapshot.HistoryScore> historyScores = getSortedHistoryScores(major.getHistoryScores());
                int numRows = Math.max(1, historyScores.size());

                for (int i = 0; i < numRows; i++) {
                    List<Object> row = new ArrayList<>();

                    if (i == 0) {
                        // 首行填充列0-10（组/专业信息）
                        row.add(group.getGroupSortOrder());
                        row.add(buildUniversityInfo(group));
                        row.add(group.getGroupCode());
                        row.add(buildGroupNameInfo(group));
                        row.add(buildDescriptionInfo(group));
                        row.add(group.getMajorCount());
                        row.add(group.getRecommendationYear());
                        row.add(formatBigDecimal(group.getRecommendationRate()));
                        row.add(major.getMajorSortOrder());
                        row.add(buildMajorNameInfo(major));
                        row.add(formatDurationTuition(major));
                    } else {
                        // 后续行列0-10为空串（待合并）
                        for (int j = 0; j < 11; j++) {
                            row.add("");
                        }
                    }

                    // 列11-18：历史分数数据
                    if (i < historyScores.size()) {
                        WishMajorSnapshot.HistoryScore score = historyScores.get(i);
                        row.add(score.getYear() != null ? score.getYear().toString() : "");
                        row.add(score.getAdmissionCount() != null ? score.getAdmissionCount().toString() : "");
                        row.add(score.getMinScore() != null ? score.getMinScore().toString() : "");
                        row.add(score.getMinRank() != null ? score.getMinRank().toString() : "");
                        row.add(formatBigDecimal(score.getAvgScore()));
                        row.add(score.getAvgRank() != null ? score.getAvgRank().toString() : "");
                        row.add(score.getMaxScore() != null ? score.getMaxScore().toString() : "");
                        row.add(score.getMaxRank() != null ? score.getMaxRank().toString() : "");
                    } else {
                        for (int j = 0; j < 8; j++) {
                            row.add("");
                        }
                    }

                    dataList.add(row);
                }

                // 若 numRows>1，添加合并区域 (startRow, startRow+numRows-1, 0, 10)
                if (numRows > 1) {
                    mergeRegions.add(new CellRangeAddress(currentRow, currentRow + numRows - 1, 0, 10));
                }
                currentRow += numRows;
            }
        }

        return dataList;
    }

    /**
     * 过滤导出专业
     * exportMajors == null：用 isExported 字段过滤（DB降级）
     * exportMajors != null：用 exportMajors.contains(id) 过滤
     */
    private List<WishMajorSnapshot> filterMajors(List<WishMajorSnapshot> majors, Set<Integer> exportMajors) {
        if (exportMajors == null) {
            return majors.stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsExported()))
                    .collect(Collectors.toList());
        } else {
            return majors.stream()
                    .filter(m -> exportMajors.contains(m.getId()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 按 year 降序排序，最多取5条
     */
    private List<WishMajorSnapshot.HistoryScore> getSortedHistoryScores(List<WishMajorSnapshot.HistoryScore> historyScores) {
        if (historyScores == null || historyScores.isEmpty()) {
            return Collections.emptyList();
        }
        return historyScores.stream()
                .sorted(Comparator.comparing(
                        WishMajorSnapshot.HistoryScore::getYear,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList());
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
     * 自定义CellWriteHandler：负责样式应用和合并区域
     * 标题行(row 0)：宋体16号，居中，绿色背景，换行
     * 表头行(row 1)：宋体13号，居中，绿色背景，换行
     * 数据行(row 2+)：宋体12号，居中，无背景，换行
     */
    private static class WishPlanStyleCellHandler implements CellWriteHandler {

        private final List<CellRangeAddress> mergeRegions;
        private WriteSheetHolder writeSheetHolder;
        private CellStyle titleStyle;
        private CellStyle headerStyle;
        private CellStyle dataStyle;

        WishPlanStyleCellHandler(List<CellRangeAddress> mergeRegions) {
            this.mergeRegions = mergeRegions;
        }

        @Override
        public void afterCellDispose(CellWriteHandlerContext context) {
            this.writeSheetHolder = context.getWriteSheetHolder();
            Cell cell = context.getCell();
            if (cell == null) {
                return;
            }

            int rowIndex = cell.getRowIndex();
            Workbook workbook = context.getWriteWorkbookHolder().getWorkbook();

            if (rowIndex == 0) {
                if (titleStyle == null) {
                    titleStyle = createStyle(workbook, 16, true);
                }
                cell.setCellStyle(titleStyle);
            } else if (rowIndex == 1) {
                if (headerStyle == null) {
                    headerStyle = createStyle(workbook, 13, true);
                }
                cell.setCellStyle(headerStyle);
            } else {
                if (dataStyle == null) {
                    dataStyle = createStyle(workbook, 12, false);
                }
                cell.setCellStyle(dataStyle);
            }
        }

        private CellStyle createStyle(Workbook workbook, int fontSize, boolean greenBackground) {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) fontSize);
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setWrapText(true);
            if (greenBackground) {
                style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            return style;
        }

        /**
         * write 完成后、finish 之前调用，通过 WriteSheetHolder 获取 POI Sheet，
         * 遍历 mergeRegions 调用 addMergedRegion
         */
        void applyMerges() {
            if (writeSheetHolder == null) {
                return;
            }
            Sheet sheet = writeSheetHolder.getSheet();
            for (CellRangeAddress region : mergeRegions) {
                sheet.addMergedRegion(region);
            }
        }
    }
}
