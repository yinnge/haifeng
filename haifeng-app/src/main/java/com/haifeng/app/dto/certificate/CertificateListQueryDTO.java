package com.haifeng.app.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端证书列表查询 DTO（spec 任务1接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateListQueryDTO extends BasePageQueryDTO {

    /** 精准查询 */
    @Size(max = 50, message = "分类长度不能超过50")
    private String category;

    /** 证书名称模糊查询 */
    @Size(max = 100, message = "证书名称长度不能超过100")
    private String certName;
}
