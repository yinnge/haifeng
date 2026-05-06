package com.haifeng.admin.vo.home;

import lombok.Data;

@Data
public class PlannerListVO {
    private Long id;
    private String name;
    private String position;
    private String region;
    private String avatar;
    private String specialty;
    private String douyinName;
    private String douyinUrl;
    private Integer sortOrder;
    private Short status;
}
