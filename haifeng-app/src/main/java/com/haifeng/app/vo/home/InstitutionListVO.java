package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InstitutionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String type;
    private String description;
    private List<String> images;
}
