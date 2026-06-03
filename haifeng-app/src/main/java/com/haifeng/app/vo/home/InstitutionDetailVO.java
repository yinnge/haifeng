package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InstitutionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String type;
    private String phone;
    private String address;
    private String description;
    private List<String> courses;
    private List<String> images;
    private String logo;
}
