package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PlannerDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String position;
    private String region;
    private String avatar;
    private String specialty;
    private String douyinName;
    private String douyinUrl;
    private String personalDescription;
    private String experienceJob;
    private List<String> achievements;
    private List<String> expertiseAreas;
}
