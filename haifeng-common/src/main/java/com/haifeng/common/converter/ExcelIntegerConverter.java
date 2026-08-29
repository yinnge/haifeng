package com.haifeng.common.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * EasyExcel Integer 转换器
 * <p>
 * 解决 EasyExcel 4.x 默认转换器无法处理以下场景的问题：
 * - 空单元格（ReadCellData type=STRING → null）
 * - 带小数的数值（450.0 → 450）
 * - 文本格式的数字（"450" → 450）
 * <p>
 * 用法：在 Integer 字段上加 @ExcelProperty(value = "列名", converter = ExcelIntegerConverter.class)
 */
public class ExcelIntegerConverter implements Converter<Integer> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        // 返回 null 表示接受所有 Excel 类型，具体类型在 convertToJavaData 中判断
        return null;
    }

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData,
                                     ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {
        if (cellData == null) {
            return null;
        }
        CellDataTypeEnum type = cellData.getType();
        if (type == null) {
            return null;
        }
        return switch (type) {
            case NUMBER -> cellData.getNumberValue().intValue();
            case STRING -> parseStringToInteger(cellData.getStringValue());
            case BOOLEAN -> cellData.getBooleanValue() ? 1 : 0;
            default -> null;
        };
    }

    private Integer parseStringToInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // 兼容 "450.0" 这类带小数的字符串
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
