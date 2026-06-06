package com.haifeng.app.vo.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/** C 端证书详情 VO（spec 任务1接口2） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
}
