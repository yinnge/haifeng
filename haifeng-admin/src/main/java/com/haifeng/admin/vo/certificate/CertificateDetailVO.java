package com.haifeng.admin.vo.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDetailVO {

    private Long id;

    private String certName;

    private String category;

    private String certLevel;

    private String applicableMajor;

    private String registrationTime;

    private String examTime;

    private Integer examFee;

    private String certIntro;

    private List<String> examRequirements;

    private String examArrangement;

    private String officialWebsite;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
