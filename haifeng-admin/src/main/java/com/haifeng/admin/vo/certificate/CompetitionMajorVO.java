package com.haifeng.admin.vo.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionMajorVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long competitionId;

    private Long majorId;

    private String majorName;

    private String competitionName;

    private OffsetDateTime createdAt;
}
