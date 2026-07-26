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
public class CompetitionListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String compName;

    private String compLevel;

    private String registrationTime;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
