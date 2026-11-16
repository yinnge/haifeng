package com.haifeng.admin.vo.home;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉选项（分类等）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionOptionVO {

    private String value;

    private String label;
}
