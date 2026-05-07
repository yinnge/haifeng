package com.haifeng.admin.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

public class StringToArrayConverter implements Converter<String[]> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return String[].class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public String[] convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                       GlobalConfiguration globalConfiguration) {
        String value = cellData.getStringValue();
        if (value == null || value.trim().isEmpty()) {
            return new String[0];
        }
        // 兼容中英文逗号
        return value.split("[,，]");
    }

    @Override
    public WriteCellData<?> convertToExcelData(String[] value, ExcelContentProperty contentProperty,
                                                GlobalConfiguration globalConfiguration) {
        if (value == null || value.length == 0) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(String.join(",", value));
    }
}
