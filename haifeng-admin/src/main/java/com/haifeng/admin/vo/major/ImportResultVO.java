package com.haifeng.admin.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultVO {

    private Integer total;

    private Integer success;

    private Integer failed;

    /**
     * 已存在记录中被"补齐空列"的条数（新增条数 = success - updated）
     */
    private Integer updated;

    private List<String> errors;
}
