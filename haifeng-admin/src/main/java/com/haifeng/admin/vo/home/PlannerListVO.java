package com.haifeng.admin.vo.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class PlannerListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String name;
    private String serviceTarget;
    private String position;
    private String province;
    private String listDesc;
    private List<String> listTags;
    private String avatarUrl;
    private Integer sortOrder;
}
