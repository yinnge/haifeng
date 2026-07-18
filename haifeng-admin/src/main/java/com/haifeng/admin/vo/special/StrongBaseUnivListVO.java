package com.haifeng.admin.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrongBaseUnivListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String universityName;
    private Boolean isPilot;
    private Short pilotYear;
    private Boolean testBeforeScore;
}
