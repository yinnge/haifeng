package com.haifeng.admin.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class OffsetDateTimeConverter implements Converter<OffsetDateTime> {

    private static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.ofHours(8);

    private static final DateTimeFormatter[] TIMEZONE_AWARE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXXX"),
    };

    private static final DateTimeFormatter[] PLAIN_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/M/d HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
    };

    @Override
    public Class<?> supportJavaTypeKey() {
        return OffsetDateTime.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public OffsetDateTime convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                            GlobalConfiguration globalConfiguration) {
        String value = cellData.getStringValue();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        value = value.trim();

        // 1. 先尝试带时区的格式
        for (DateTimeFormatter formatter : TIMEZONE_AWARE_FORMATS) {
            try {
                return OffsetDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        // 2. 再尝试无时区的格式，解析为 LocalDateTime 后补上东八区偏移
        for (DateTimeFormatter formatter : PLAIN_FORMATS) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(value, formatter);
                return ldt.atOffset(DEFAULT_OFFSET);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    @Override
    public WriteCellData<?> convertToExcelData(OffsetDateTime value, ExcelContentProperty contentProperty,
                                                GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
