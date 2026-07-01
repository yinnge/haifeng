package com.haifeng.app.vo.university;

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
public class UniversityChannelListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String channelCode;
    private String channelName;
    private Short year;
    private String regionTag;
    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;
}
