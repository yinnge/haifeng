package com.haifeng.app.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelUnivListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long universityId;
    private String universityName;
    private Short year;
    private String regionTag;
    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;
}
