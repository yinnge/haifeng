package com.haifeng.admin.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class OffsetDateTimeConverter implements Converter<OffsetDateTime> {

    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
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
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return OffsetDateTime.parse(value, formatter);
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
