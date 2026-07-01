package com.haifeng.app.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrongBaseUniversityDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
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
}
