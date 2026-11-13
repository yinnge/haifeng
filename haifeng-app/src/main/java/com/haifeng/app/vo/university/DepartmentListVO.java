package com.haifeng.app.vo.university;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端院系列表 VO（spec §3.3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String departmentName;
    private String departmentType;
}
