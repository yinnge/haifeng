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

    /**
     * 上下架状态（派生字段，不落库）：
     * {@code 1} = 在线（is_deleted = false），{@code 0} = 已下线（is_deleted = true）。
     */
    private Short status;

    /** 绑定的教师账号用户名（派生字段，便于列表直接展示） */
    private String teacherUsername;
}
