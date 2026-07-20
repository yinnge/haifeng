package com.haifeng.app.dto.algorithm.pdf;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PdfRecordQueryDTO extends BasePageQueryDTO {

    /** 按状态过滤：0=生成中, 1=成功, 2=失败 */
    private Short status;

    /** 按志愿方案ID过滤 */
    private Integer planId;
}
