package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlannerListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String region;
    private String position;
    private String avatar;
    private String specialty;
    private String personalDescription;
}
