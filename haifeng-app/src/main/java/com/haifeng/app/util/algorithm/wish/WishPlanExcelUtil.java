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
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /** 总列数：组号|安全系数|专业组信息|大学信息|专业数量|推免年份|推免率|序号|专业名称|学制/学费|年份|计划招生人数|最低分|最低位次|平均分|平均位次|最高分|最高位次 */
    private static final int TOTAL_COLUMNS = 18;

    /** 列索引常量 */
    private static final int COL_GROUP_NO = 0;
    private static final int COL_SAFETY = 1;
    private static final int COL_GROUP_INFO = 2;
    private static final int COL_UNIVERSITY = 3;
    private static final int COL_MAJOR_COUNT = 4;
    private static final int COL_RECOMMEND_YEAR = 5;
    private static final int COL_RECOMMEND_RATE = 6;
    private static final int COL_SEQ = 7;
    private static final int COL_MAJOR_NAME = 8;
    private static final int COL_DURATION_TUITION = 9;
    private static final int COL_YEAR = 10;
    private static final int COL_PLAN_COUNT = 11;
    private static final int COL_MIN_SCORE = 12;
    private static final int COL_MIN_RANK = 13;
    private static final int COL_AVG_SCORE = 14;
    private static final int COL_AVG_RANK = 15;
    private static final int COL_MAX_SCORE = 16;
    private static final int COL_MAX_RANK = 17;

    /** 档位简写 → POI 索引色（与前端 PlanDetail.vue safetyColorMap 尽量接近） */
    private static final Map<String, Short> SAFETY_COLOR_MAP = Map.of(
            "搏", IndexedColors.RED.getIndex(),
            "冲", IndexedColors.LIGHT_ORANGE.getIndex(),
            "稳", IndexedColors.ORANGE.getIndex(),
            "保", IndexedColors.GREEN.getIndex(),
            "垫", IndexedColors.BLUE.getIndex()
    );

    /** 选科颜色（与需求一致：不限黑、二选一/三选一蓝、必选红） */
    private static final Short COLOR_RED = IndexedColors.RED.getIndex();
    private static final Short COLOR_BLUE = IndexedColors.BLUE.getIndex();

    /** 单元格基准字号（与数据行 12 号一致）；大学名/专业名为 14 号 */
    private static final short BASE_FONT_SIZE = 12;
    /** 安全系数 short(档位简写) 字号：在基准上放大两个字号 = 14 */
    private static final short SAFETY_SHORT_SIZE = 14;
    /** 安全系数 level(百分比) 描述字号：在基准上放大约一个字号 = 13 */
    private static final short SAFETY_LEVEL_SIZE = 13;

    /** 列宽相关常量（POI 列宽单位：1/256 个字符宽度） */
    private static final int WIDTH_UNITS_PER_CHAR = 256;
    private static final int NARROW_COLUMN_WIDTH_CHARS = 8;
    private static final int NARROW_COLUMN_WIDTH_UNITS = NARROW_COLUMN_WIDTH_CHARS * WIDTH_UNITS_PER_CHAR;
    private static final int MEDIUM_COLUMN_WIDTH_CHARS = 12;
    private static final int MEDIUM_COLUMN_WIDTH_UNITS = MEDIUM_COLUMN_WIDTH_CHARS * WIDTH_UNITS_PER_CHAR;
    private static final int PLAN_COUNT_COLUMN_WIDTH_CHARS = 16;
    private static final int PLAN_COUNT_COLUMN_WIDTH_UNITS = PLAN_COUNT_COLUMN_WIDTH_CHARS * WIDTH_UNITS_PER_CHAR;
    private static final int TUITION_COLUMN_WIDTH_CHARS = 24;
    private static final int TUITION_COLUMN_WIDTH_UNITS = TUITION_COLUMN_WIDTH_CHARS * WIDTH_UNITS_PER_CHAR;
    private static final int COMBINED_COLUMNS_FOR_WIDE = 5;
    private static final int WIDE_COLUMN_WIDTH_UNITS = COMBINED_COLUMNS_FOR_WIDE * NARROW_COLUMN_WIDTH_UNITS;
    private static final float ROW_HEIGHT_PER_LINE = 18.0f;

    /**
     * 导出志愿方案到Excel
     * 流程：先由 EasyExcel 写出（含合并/边框/列宽/行高），再用 POI XSSF 二次读回，
     * 对“安全系数/专业组信息/大学信息/专业名称”四列直接套富文本（规避 EasyExcel List<List> 路径把富文本降级为纯文本的问题）。
     */
    public void exportToExcel(OutputStream outputStream,
                              WishPlan wishPlan,
                              List<WishGroupSnapshot> groups,
                              Map<Integer, List<WishMajorSnapshot>> majorsMap,
                              Set<Integer> exportMajors) {
        try {
            List<CellRangeAddress> mergeRegions = new ArrayList<>();
            Map<String, RichTextSpec> richSpecMap = new HashMap<>();
            WishPlanStyleCellHandler styleCellHandler = new WishPlanStyleCellHandler(mergeRegions);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ExcelWriter excelWriter = EasyExcel.write(baos)
                    .registerWriteHandler(styleCellHandler)
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet("志愿方案").build();

            List<List<Object>> dataList = buildDataList(wishPlan, groups, majorsMap, exportMajors, mergeRegions, richSpecMap);
            excelWriter.write(dataList, writeSheet);
            styleCellHandler.applyMerges();
            excelWriter.finish();

            // 第二遍：用 POI XSSF 直接写富文本（EasyExcel 的 List<List> 写入路径会把富文本降级为纯文本，故二次覆盖）
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));
            applyRichText(xssfWorkbook, richSpecMap);
            xssfWorkbook.write(outputStream);
            xssfWorkbook.close();
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new BusinessException(ResultCode.EXPORT_FAILED);
        }
    }

    /**
     * 把富文本规格应用到具体单元格：索引 key 为 "row:col"
     */
    private void applyRichText(XSSFWorkbook workbook, Map<String, RichTextSpec> richSpecMap) {
        Sheet sheet = workbook.getSheetAt(0);
        for (Map.Entry<String, RichTextSpec> entry : richSpecMap.entrySet()) {
            String[] parts = entry.getKey().split(":");
            int rowIndex = Integer.parseInt(parts[0]);
            int colIndex = Integer.parseInt(parts[1]);
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            Cell oldCell = row.getCell(colIndex);
            if (oldCell == null) {
                continue;
            }
            RichTextSpec spec = entry.getValue();
            XSSFRichTextString rich = new XSSFRichTextString(spec.text);
            for (RichTextSegment seg : spec.segments) {
                XSSFFont font = workbook.createFont();
                font.setFontName("宋体");
                if (seg.colorIndex != null) {
                    font.setColor(seg.colorIndex);
                }
                if (seg.bold) {
                    font.setBold(true);
                }
                // 未指定字号时统一用基准字号；大学名/专业名显式 14 号；安全系数 level 描述显式放大两号
                font.setFontHeightInPoints(seg.size > 0 ? seg.size : BASE_FONT_SIZE);
                rich.applyFont(seg.start, seg.end, font);
            }
            // EasyExcel 输出为 inline-string 单元格，直接 setCellValue(richText) 会被序列化回纯文本而丢失富文本 run。
            // 因此删除原单元格并新建一个（默认走共享字符串），从而保留 <r> 富文本节点；同时继承原边框样式。
            CellStyle originalStyle = oldCell.getCellStyle();
            row.removeCell(oldCell);
            Cell newCell = row.createCell(colIndex);
            newCell.setCellStyle(originalStyle);
            newCell.setCellValue(rich);
        }
    }

    private List<List<Object>> buildDataList(WishPlan wishPlan,
                                             List<WishGroupSnapshot> groups,
                                             Map<Integer, List<WishMajorSnapshot>> majorsMap,
                                             Set<Integer> exportMajors,
                                             List<CellRangeAddress> mergeRegions,
                                             Map<String, RichTextSpec> richSpecMap) {
        List<List<Object>> dataList = new ArrayList<>();

        // 行0：标题行（合并所有列）
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
        for (int i = 1; i < TOTAL_COLUMNS; i++) {
            titleRow.add("");
        }
        dataList.add(titleRow);
        mergeRegions.add(new CellRangeAddress(0, 0, 0, TOTAL_COLUMNS - 1));

        // 行1：表头行
        List<Object> headerRow = new ArrayList<>();
        headerRow.add("组号");
        headerRow.add("安全系数");
        headerRow.add("专业组信息");
        headerRow.add("大学信息");
        headerRow.add("专业数量");
        headerRow.add("推免年份");
        headerRow.add("推免率");
        headerRow.add("序号");
        headerRow.add("专业名称");
        headerRow.add("学制/学费");
        headerRow.add("年份");
        headerRow.add("计划招生人数");
        headerRow.add("最低分");
        headerRow.add("最低位次");
        headerRow.add("平均分");
        headerRow.add("平均位次");
        headerRow.add("最高分");
        headerRow.add("最高位次");
        dataList.add(headerRow);

        // 行2+：数据行
        int currentRow = 2;

        for (WishGroupSnapshot group : groups) {
            List<WishMajorSnapshot> majors = majorsMap.getOrDefault(group.getId(), Collections.emptyList());
            List<WishMajorSnapshot> filteredMajors = filterMajors(majors, exportMajors);

            if (filteredMajors.isEmpty()) {
                continue;
            }

            int groupSeq = 0; // 组内专业序号：每个组号从 1 开始自增

            int groupStartRow = currentRow;
            // 组级富文本（安全系数 / 专业组信息 / 大学信息）仅首行写入，并记录到 map 供二次读回套富文本
            RichTextSpec safetySpec = buildSafetyText(filteredMajors);
            RichTextSpec groupInfoSpec = buildGroupInfo(group);
            RichTextSpec universitySpec = buildCombinedUniversityInfo(group);
            richSpecMap.put(groupStartRow + ":" + COL_SAFETY, safetySpec);
            richSpecMap.put(groupStartRow + ":" + COL_GROUP_INFO, groupInfoSpec);
            richSpecMap.put(groupStartRow + ":" + COL_UNIVERSITY, universitySpec);

            for (WishMajorSnapshot major : filteredMajors) {
                groupSeq++;
                List<WishMajorSnapshot.HistoryScore> historyScores = getSortedHistoryScores(major.getHistoryScores());
                int numRows = Math.max(1, historyScores.size());
                int majorStartRow = currentRow;

                // 专业级富文本（专业名称）首行写入，记录到 map
                RichTextSpec majorSpec = buildMajorNameInfo(major);
                richSpecMap.put(majorStartRow + ":" + COL_MAJOR_NAME, majorSpec);

                for (int i = 0; i < numRows; i++) {
                    List<Object> row = new ArrayList<>();

                    if (i == 0) {
                        // 首行：填充组级 + 专业级列
                        row.add(group.getGroupSortOrder());
                        row.add(safetySpec.text);
                        row.add(groupInfoSpec.text);
                        row.add(universitySpec.text);
                        row.add(group.getMajorCount());
                        row.add(group.getRecommendationYear());
                        row.add(formatPercent(group.getRecommendationRate()));
                        row.add(groupSeq);
                        row.add(majorSpec.text);
                        row.add(formatDurationTuition(major));
                    } else {
                        // 后续行：这些列留空（待合并）
                        for (int j = 0; j <= COL_DURATION_TUITION; j++) {
                            row.add("");
                        }
                    }

                    // 年份 + 分数数据（9-16）
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
                    currentRow++;
                }

                // 每专业内合并：序号(6)、专业名称(7)、学制/学费(8) 跨年份行
                if (numRows > 1) {
                    mergeRegions.add(new CellRangeAddress(majorStartRow, majorStartRow + numRows - 1, COL_SEQ, COL_SEQ));
                    mergeRegions.add(new CellRangeAddress(majorStartRow, majorStartRow + numRows - 1, COL_MAJOR_NAME, COL_MAJOR_NAME));
                    mergeRegions.add(new CellRangeAddress(majorStartRow, majorStartRow + numRows - 1, COL_DURATION_TUITION, COL_DURATION_TUITION));
                }
            }

            int groupEndRow = currentRow - 1;

            // 每组合并：组号、安全系数、专业组信息、大学信息、专业数量、推免年份、推免率
            if (groupEndRow > groupStartRow) {
                mergeRegions.add(new CellRangeAddress(groupStartRow, groupEndRow, COL_GROUP_NO, COL_GROUP_NO));
                mergeRegions.add(new CellRangeAddress(groupStartRow, groupEndRow, COL_SAFETY, COL_SAFETY));
                mergeRegions.add(new CellRangeAddress(groupStartRow, groupEndRow, COL_GROUP_INFO, COL_GROUP_INFO));
                mergeRegions.add(new CellRangeAddress(groupStartRow, groupEndRow, COL_UNIVERSITY, COL_UNIVERSITY));
                mergeRegions.add(new CellRangeAddress(groupStartRow, groupEndRow, COL_MAJOR_COUNT, COL_MAJOR_COUNT));
                mergeRegions.add(new CellRangeAddress(groupStartRow, groupEndRow, COL_RECOMMEND_YEAR, COL_RECOMMEND_YEAR));
                mergeRegions.add(new CellRangeAddress(groupStartRow, groupEndRow, COL_RECOMMEND_RATE, COL_RECOMMEND_RATE));
            }
        }

        return dataList;
    }

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

    /**
     * 推免率格式化：库里存的是整数百分比（如 56.00 表示 56%），直接拼百分号。
     * 既不要裸数字（56），也不要比率形式（0.56）。
     */
    private String formatPercent(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString() + "%";
    }

    private String formatDurationTuition(WishMajorSnapshot major) {
        // 学制兼容：数据库可能存 "4" 或 "4年"，统一去掉已有"年"字后追加"年"，避免重复或缺失单位
        String rawDuration = major.getDuration() != null ? major.getDuration().trim() : "";
        String duration;
        if (rawDuration.isEmpty()) {
            duration = "";
        } else {
            String normalized = rawDuration.endsWith("年")
                    ? rawDuration.substring(0, rawDuration.length() - 1)
                    : rawDuration;
            duration = normalized + "年";
        }
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

    /**
     * 3.1/4.1 专业名称：【专业名】专业代码：【专业代码】 + 描述
     * 专业名：加粗 + 大一号；专业代码后空一行再接描述
     */
    private RichTextSpec buildMajorNameInfo(WishMajorSnapshot major) {
        RichTextBuilder b = new RichTextBuilder();
        // 3.1 专业名 加粗 + 大一号
        b.append(wrapBracket(major.getMajorName()), null, true, (short) 14);
        b.append("\n");
        if (major.getMajorCode() != null) {
            b.append("专业代码：" + wrapBracket(major.getMajorCode()), null, false, (short) 0);
            b.append("\n\n"); // 3.2 与描述之间空一行
        }
        if (major.getDescription() != null && !major.getDescription().isEmpty()) {
            b.append(major.getDescription(), null, false, (short) 0);
        }
        return new RichTextSpec(b.text(), b.segments());
    }

    /**
     * 3.2 选科要求文案，与前端 GroupRow.vue requirementText 逻辑一致：
     * 不限 → 不限；必选1 → 必选【科目】；必选2/3 → 【A】和【B】(和【C】)必选；
     * 2选1/3选1 → 【A】和【B】(和【C】)选一
     */
    private String buildRequirementText(String requirementType, List<String> subjects) {
        String type = requirementType == null ? "" : requirementType;
        List<String> list = subjects == null ? Collections.emptyList() : subjects;
        if (type.isEmpty() || "不限".equals(type)) {
            return "不限";
        }
        java.util.regex.Matcher must = java.util.regex.Pattern.compile("^必选(\\d+)$").matcher(type);
        if (must.find()) {
            int n = Integer.parseInt(must.group(1));
            List<String> picked = list.subList(0, Math.min(n, list.size()));
            if (picked.isEmpty()) {
                return type;
            }
            if (n <= 1) {
                return "必选" + wrapBracket(picked.get(0));
            }
            return picked.stream().map(WishPlanExcelUtil::wrapBracket).collect(Collectors.joining("和")) + "必选";
        }
        java.util.regex.Matcher pick = java.util.regex.Pattern.compile("^(\\d+)选(\\d+)$").matcher(type);
        if (pick.find()) {
            int m = Integer.parseInt(pick.group(1));
            int n = Integer.parseInt(pick.group(2));
            List<String> picked = list.subList(0, Math.min(m, list.size()));
            if (picked.isEmpty()) {
                return type;
            }
            String[] cn = {"零", "一", "二", "三", "四", "五", "六"};
            String cnStr = n < cn.length ? cn[n] : String.valueOf(n);
            return picked.stream().map(WishPlanExcelUtil::wrapBracket).collect(Collectors.joining("和")) + "选" + cnStr;
        }
        return type;
    }

    /**
     * 选科整段颜色：不限→黑(null)；二选一/三选一→蓝；必选1/2/3→红
     */
    private Short requirementColor(String requirementType) {
        String type = requirementType == null ? "" : requirementType;
        if (type.isEmpty() || "不限".equals(type)) {
            return null;
        }
        if (type.matches("^\\d+选\\d+$")) {
            return COLOR_BLUE;
        }
        if (type.matches("^必选\\d+$")) {
            return COLOR_RED;
        }
        return null;
    }

    /**
     * 【】包装，null/空返回空串
     */
    private static String wrapBracket(String s) {
        return s == null || s.isEmpty() ? "" : "【" + s + "】";
    }

    /**
     * 专业组信息（独立列，与安全系数/大学信息同组合并）：
     * 专业组名:【】 独立行；省招代码:【】 独立行。其余（选科等）仍留在大学信息列。
     */
    private RichTextSpec buildGroupInfo(WishGroupSnapshot group) {
        RichTextBuilder b = new RichTextBuilder();
        if (group.getGroupName() != null) {
            b.append("专业组名:" + wrapBracket(group.getGroupName()), null, false, (short) 0);
            b.append("\n");
        }
        if (group.getEnrollmentCode() != null) {
            b.append("省招代码:" + wrapBracket(group.getEnrollmentCode()), null, false, (short) 0);
            b.append("\n");
        }
        return new RichTextSpec(b.text(), b.segments());
    }

    /**
     * 组合大学信息（富文本，分片段着色）：
     * 2.10 【大学名】加粗+大一号；2.2 城市换行后展示性质(公办/民办)；2.3 标签后换行；
     * 2.7 选科按规则着色；2.8 选科与描述间空一行；2.9 描述与限制间空一行；2.1 仅“限制”为红色，其余黑色。
     * 注：专业组名/省招代码已拆到独立的“专业组信息”列。
     */
    private RichTextSpec buildCombinedUniversityInfo(WishGroupSnapshot group) {
        RichTextBuilder b = new RichTextBuilder();
        // 2.10 大学名 加粗 + 大一号（含【】）
        b.append(wrapBracket(group.getUniversityName()), null, true, (short) 14);
        b.append("\n");
        // 2.2 城市+性质+类别 合并到同一行：【北京】【公办】【综合】
        b.append(wrapBracket(group.getCityName()), null, false, (short) 0);
        b.append(wrapBracket(group.getNature()), null, false, (short) 0);
        b.append(wrapBracket(group.getCategory()), null, false, (short) 0);
        b.append("\n");
        if (group.getTags() != null && !group.getTags().isEmpty()) {
            b.append(wrapBracket(String.join(",", group.getTags())), null, false, (short) 0);
            b.append("\n"); // 2.3 标签后换行
        }
        // 2.6/2.7 选科（按规则着色）
        String reqText = buildRequirementText(group.getRequirementType(), group.getSubjects());
        Short reqColor = requirementColor(group.getRequirementType());
        b.append("选科:" + reqText, reqColor, false, (short) 0);
        b.append("\n\n"); // 2.8 与描述间空一行
        // 描述（不加【】）
        if (group.getDescription() != null && !group.getDescription().isEmpty()) {
            b.append(group.getDescription(), null, false, (short) 0);
            b.append("\n\n"); // 2.9 与限制间空一行
        }
        // 2.1 限制：红色
        if (group.getConstraintsDescription() != null && !group.getConstraintsDescription().isEmpty()) {
            b.append("限制：" + String.join("，", group.getConstraintsDescription()), COLOR_RED, false, (short) 0);
        }
        return new RichTextSpec(b.text(), b.segments());
    }

    /**
     * 组级安全系数：大字 levelShort（搏/冲/稳/保/垫），小字 safetyLevel × 100%
     * 取组内 safetyLevel 最大的专业。仅 levelShort 着色，百分比保持黑色。
     */
    private RichTextSpec buildSafetyText(List<WishMajorSnapshot> majors) {
        WishMajorSnapshot top = null;
        BigDecimal maxLevel = BigDecimal.ZERO;
        for (WishMajorSnapshot m : majors) {
            if (m.getSafetyLevel() != null && m.getSafetyLevel().compareTo(maxLevel) > 0) {
                maxLevel = m.getSafetyLevel();
                top = m;
            }
        }
        if (top == null) {
            return new RichTextSpec("", Collections.emptyList());
        }
        String levelShort = top.getLevelShort() != null ? top.getLevelShort() : "";
        String percentage = maxLevel.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toString() + "%";
        String text = levelShort + "\n" + percentage;
        List<RichTextSegment> segments = new ArrayList<>();
        // 只给短词(levelShort)上色并放大两号；百分比保持默认黑色，放大约一个字号
        Short color = SAFETY_COLOR_MAP.get(levelShort);
        if (color != null) {
            segments.add(new RichTextSegment(0, levelShort.length(), color, false, SAFETY_SHORT_SIZE));
        }
        int pctStart = levelShort.length() + 1; // +1 跳过换行符
        segments.add(new RichTextSegment(pctStart, pctStart + percentage.length(), null, false, SAFETY_LEVEL_SIZE));
        return new RichTextSpec(text, segments);
    }

    /**
     * 计算列宽（POI 单位 = 1/256 字符宽度），固定宽度方案：
     * 大学信息、专业名称 = 5 个窄列宽；学制/学费 = 3 倍窄列宽；
     * 安全系数/专业数量/推免年份/最低位次/平均位次/最高位次 = 中等宽度；
     * 计划招生人数 = 加宽；其余 = 窄宽。
     */
    private static int computeColumnWidth(int colIndex) {
        switch (colIndex) {
            case COL_UNIVERSITY:
            case COL_MAJOR_NAME:
            case COL_GROUP_INFO:
                return WIDE_COLUMN_WIDTH_UNITS;
            case COL_DURATION_TUITION:
                return TUITION_COLUMN_WIDTH_UNITS;
            case COL_SAFETY:
            case COL_MAJOR_COUNT:
            case COL_RECOMMEND_YEAR:
            case COL_MIN_RANK:
            case COL_AVG_RANK:
            case COL_MAX_RANK:
                return MEDIUM_COLUMN_WIDTH_UNITS;
            case COL_PLAN_COUNT:
                return PLAN_COUNT_COLUMN_WIDTH_UNITS;
            default:
                return NARROW_COLUMN_WIDTH_UNITS;
        }
    }

    /**
     * 估算合并单元格内文字需要多少行。
     * 按实际换行符分割，并考虑中文字符宽度约为 ASCII 两倍的折行。
     */
    private static int estimateLineCount(String text, int colWidthUnits) {
        if (text == null || text.isEmpty()) {
            return 1;
        }
        double capacity = Math.max(colWidthUnits / 256.0, 1.0); // 列宽可容纳的 ASCII 字符数
        int totalLines = 0;
        for (String line : text.split("\\n", -1)) {
            double units = 0;
            for (char c : line.toCharArray()) {
                units += isChineseChar(c) ? 2.0 : 1.0;
            }
            int wrappedLines = units <= 0 ? 1 : (int) Math.ceil(units / capacity);
            totalLines += Math.max(1, wrappedLines);
        }
        return Math.max(1, totalLines);
    }

    private static boolean isChineseChar(char c) {
        // CJK Unified Ideographs + 扩展A/B/C/D + 兼容区
        return (c >= '\u4e00' && c <= '\u9fff')
                || (c >= '\u3400' && c <= '\u4dbf')
                || (c >= '\uf900' && c <= '\ufaff');
    }

    /**
     * 自定义CellWriteHandler：负责样式应用、合并区域、列宽、行高
     * 标题行(row 0)：宋体16号，居中，绿色背景，黑色边框
     * 表头行(row 1)：宋体13号，居中，绿色背景，黑色边框；同时按固定方案设置列宽
     * 数据行(row 2+)：宋体12号，居中，无背景，绿色边框
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
                    titleStyle = createStyle(workbook, 16, true, true);
                }
                cell.setCellStyle(titleStyle);
            } else if (rowIndex == 1) {
                if (headerStyle == null) {
                    headerStyle = createStyle(workbook, 13, true, true);
                }
                cell.setCellStyle(headerStyle);
                // 表头行设置固定列宽
                if (writeSheetHolder != null && cell.getCellType() == CellType.STRING) {
                    int colIndex = cell.getColumnIndex();
                    writeSheetHolder.getSheet().setColumnWidth(colIndex, computeColumnWidth(colIndex));
                }
            } else {
                if (dataStyle == null) {
                    dataStyle = createStyle(workbook, 12, false, true);
                }
                cell.setCellStyle(dataStyle);
            }
        }

        private CellStyle createStyle(Workbook workbook, int fontSize, boolean greenBackground, boolean withBorder) {
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
            if (withBorder) {
                // 黑色边框（表头/标题）/ 绿色边框（数据）
                short borderColor = withBorder && !greenBackground
                        ? IndexedColors.GREEN.getIndex()
                        : IndexedColors.BLACK.getIndex();
                style.setBorderTop(BorderStyle.THIN);
                style.setBorderBottom(BorderStyle.THIN);
                style.setBorderLeft(BorderStyle.THIN);
                style.setBorderRight(BorderStyle.THIN);
                style.setTopBorderColor(borderColor);
                style.setBottomBorderColor(borderColor);
                style.setLeftBorderColor(borderColor);
                style.setRightBorderColor(borderColor);
            }
            return style;
        }

        void applyMerges() {
            if (writeSheetHolder == null) {
                return;
            }
            Sheet sheet = writeSheetHolder.getSheet();
            for (CellRangeAddress region : mergeRegions) {
                sheet.addMergedRegion(region);
            }

            // 收集大学信息 / 专业名称 / 专业组信息 合并区的左上角坐标
            Set<String> mergedTopLefts = new HashSet<>();
            for (CellRangeAddress region : mergeRegions) {
                if (region.getFirstColumn() == COL_UNIVERSITY
                        || region.getFirstColumn() == COL_MAJOR_NAME
                        || region.getFirstColumn() == COL_GROUP_INFO) {
                    mergedTopLefts.add(region.getFirstRow() + ":" + region.getFirstColumn());
                }
            }

            // 自适应行高：大学信息 / 专业名称 / 专业组信息 列按文字量与列宽计算所需行高
            int lastRow = sheet.getLastRowNum();
            for (int r = 2; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                for (int col : new int[]{COL_UNIVERSITY, COL_MAJOR_NAME, COL_GROUP_INFO}) {
                    Cell cell = row.getCell(col);
                    if (cell == null || cell.getCellType() != CellType.STRING) {
                        continue;
                    }
                    String text = cell.getStringCellValue();
                    if (text == null || text.isEmpty()) {
                        continue;
                    }
                    String key = r + ":" + col;
                    boolean isTopLeft = mergedTopLefts.contains(key);
                    if (!isTopLeft && isInsideMergeRegion(r, col)) {
                        // 合并区内部的重复单元格（组内每个专业首行都会写入组级文本），跳过
                        continue;
                    }
                    int colWidthUnits = sheet.getColumnWidth(col);
                    int lineCount = estimateLineCount(text, colWidthUnits);
                    if (isTopLeft) {
                        // 合并区：在合并范围内均匀分配总高度
                        for (CellRangeAddress region : mergeRegions) {
                            if (region.getFirstRow() == r && region.getFirstColumn() == col) {
                                int rowSpan = region.getLastRow() - region.getFirstRow() + 1;
                                float perRowHeight = (lineCount * ROW_HEIGHT_PER_LINE) / rowSpan;
                                for (int rr = region.getFirstRow(); rr <= region.getLastRow(); rr++) {
                                    Row targetRow = sheet.getRow(rr);
                                    if (targetRow == null) {
                                        continue;
                                    }
                                    targetRow.setHeightInPoints(Math.max(targetRow.getHeightInPoints(), perRowHeight));
                                }
                                break;
                            }
                        }
                    } else {
                        // 单行单元格：直接设置本行高度
                        float height = lineCount * ROW_HEIGHT_PER_LINE;
                        row.setHeightInPoints(Math.max(row.getHeightInPoints(), height));
                    }
                }
            }
        }

        private boolean isInsideMergeRegion(int row, int col) {
            for (CellRangeAddress region : mergeRegions) {
                if (region.getFirstColumn() == col
                        && region.getFirstRow() <= row && row <= region.getLastRow()) {
                    return true;
                }
            }
            return false;
        }
    }

    /** 富文本片段：start/end 为字符下标（含换行符），colorIndex 为 POI 索引色（null=默认黑），bold/size 可选 */
    private static class RichTextSegment {
        int start;
        int end;
        Short colorIndex;
        boolean bold;
        short size;
        RichTextSegment(int start, int end, Short colorIndex, boolean bold, short size) {
            this.start = start;
            this.end = end;
            this.colorIndex = colorIndex;
            this.bold = bold;
            this.size = size;
        }
    }

    /** 一个单元格的富文本：纯文本 + 片段样式列表 */
    private static class RichTextSpec {
        String text;
        List<RichTextSegment> segments;
        RichTextSpec(String text, List<RichTextSegment> segments) {
            this.text = text;
            this.segments = segments;
        }
    }

    /** 富文本构造器：顺序追加，记录带样式的片段下标 */
    private static class RichTextBuilder {
        private final StringBuilder sb = new StringBuilder();
        private final List<RichTextSegment> segs = new ArrayList<>();
        void append(String text) {
            if (text != null) {
                sb.append(text);
            }
        }
        void append(String text, Short colorIndex, boolean bold, short size) {
            if (text == null || text.isEmpty()) {
                return;
            }
            int start = sb.length();
            sb.append(text);
            int end = sb.length();
            segs.add(new RichTextSegment(start, end, colorIndex, bold, size));
        }
        String text() {
            return sb.toString();
        }
        List<RichTextSegment> segments() {
            return segs;
        }
    }
}
