package com.haifeng.app.vo.city;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityBriefVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String cityName;
    private String province;
    private String region;
    private String cityIntro;
    private Integer collegeCount;
}
