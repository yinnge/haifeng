package com.haifeng.app.vo.algorithm.pdf;

import com.haifeng.app.vo.city.CityDetailVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 城市维度 PDF 聚合结果
 * <p>同一城市下可能有多个专业组（多所院校），detail 由 {@link com.haifeng.app.service.city.CityService#detailByName} 提供。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfCityVO {

    /** 城市名（唯一） */
    private String cityName;

    /** 城市详情（TODO：aggregateCities 中调用 CityService.detailByName 填充） */
    private CityDetailVO detail;

    /** 该城市下的专业组（含院校与可导出专业） */
    private List<ExportGroupContextVO> groups;
}
