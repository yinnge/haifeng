package com.haifeng.admin.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateQueryDTO extends BasePageQueryDTO {
    private String certName;
    private String category;
    private String certLevel;
    private String applicableMajor;
    private Boolean isDeleted;
}
