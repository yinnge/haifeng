package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;

    private String provinceName;

    private String cityName;

    private String region;

    private String category;

    private Integer majorCount;

    private String educationLevel;

    private String nature;

    private Integer status;

    private LocalDateTime createdAt;
}
