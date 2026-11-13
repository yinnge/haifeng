package com.haifeng.app.vo.major;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端考研专业列表 VO（spec 任务2接口1） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgradMajorListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String majorName;
    private String majorCode;
    private String degreeType;
    private String disciplineCategory;
    private String popularity;
    private String difficulty;
    private String brief;
    private String[] examSubjects;
}
