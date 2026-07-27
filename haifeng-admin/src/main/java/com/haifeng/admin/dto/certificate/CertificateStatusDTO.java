package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificateStatusDTO {

    @NotNull(message = "状态不能为空")
    private Boolean isDeleted;
}
