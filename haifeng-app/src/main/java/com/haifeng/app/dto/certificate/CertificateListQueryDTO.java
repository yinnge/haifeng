package com.haifeng.app.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端证书列表查询 DTO（spec 任务1接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateListQueryDTO extends BasePageQueryDTO {

    /** 精准查询 */
    private String category;

    /** 证书名称模糊查询 */
    private String certName;
}
