package com.haifeng.admin.vo.dashboard;

import lombok.Data;
import java.util.List;

@Data
public class TrendDataVO {
    /** 日期列表，格式 yyyy-MM-dd */
    private List<String> dates;
    /** 对应日期的数值列表 */
    private List<Long> values;
}
