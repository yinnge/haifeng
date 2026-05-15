package com.haifeng.app.vo.city;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityBriefVO {
    private String cityName;
    private String province;
    private String region;
    private String cityIntro;
    private Integer collegeCount;
}
