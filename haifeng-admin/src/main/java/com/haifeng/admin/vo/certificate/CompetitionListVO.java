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
public class CompetitionListVO {

    private Long id;

    private String compName;

    private String compLevel;

    private String registrationTime;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
