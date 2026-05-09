package com.haifeng.admin.vo.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionMajorVO {

    private Long id;

    private Long competitionId;

    private Long majorId;

    private String majorName;

    private String competitionName;

    private OffsetDateTime createdAt;
}
