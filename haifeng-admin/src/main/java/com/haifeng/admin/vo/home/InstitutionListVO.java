package com.haifeng.admin.vo.home;

import lombok.Data;

@Data
public class InstitutionListVO {
    private Long id;
    private String name;
    private String type;
    private String phone;
    private String address;
    private String logo;
    private Integer sortOrder;
    private Short status;
}
