package com.haifeng.admin.excel.university;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.util.Arrays;
import java.util.List;

public class StringArrayConverter implements Converter<List<String>> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return List.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public List<String> convertToJavaData(ReadCellData<?> cellData,
                                          ExcelContentProperty contentProperty,
                                          GlobalConfiguration globalConfiguration) {
        String val = cellData.getStringValue();
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        // 支持中英文逗号分隔
        String[] arr = val.split("[,，]");
        return Arrays.stream(arr).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
