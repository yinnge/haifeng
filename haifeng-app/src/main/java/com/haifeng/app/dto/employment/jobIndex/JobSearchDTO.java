package com.haifeng.app.dto.employment.jobIndex;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String province;

    private String city;

    private String educationRequirement;

    private String recruitmentType;

    private Integer salaryMin;

    private Integer salaryMax;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDateStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDateEnd;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate regDeadlineStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate regDeadlineEnd;

    private String positionStatus;

    private String categoryLabel;
}
