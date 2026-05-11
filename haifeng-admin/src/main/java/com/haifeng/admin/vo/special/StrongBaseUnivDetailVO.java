package com.haifeng.admin.vo.special;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class StrongBaseUnivDetailVO {
    private Long id;
    private Long universityId;
    private String universityName;
    private Boolean isPilot;
    private Short pilotYear;
    private String officialUrl;
    private String signupUrl;
    private Boolean testBeforeScore;
    private String defaultEntryRatio;
    private String defaultAdmissionFormula;
    private String[] availableMajors;
    private String specialNotes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
