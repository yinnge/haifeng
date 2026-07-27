package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CertificateBatchStatusDTO {

    @NotEmpty(message = "证书ID列表不能为空")
    private List<Long> ids;

    @NotNull(message = "状态不能为空")
    private Boolean isDeleted;
}
