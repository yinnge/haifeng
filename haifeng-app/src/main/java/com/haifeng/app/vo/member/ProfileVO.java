package com.haifeng.app.vo.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVO {

    private String realName;

    private String email;

    private String gender;

    private String schoolName;

    private String province;

    private String city;

    private String major;

    private String identity;

    private String grade;

    private String educationLevel;

    private Integer favoriteCount;

    private Integer viewCount;

    /**
     * 是否可以填写学校（仅大学生/研究生可填）
     */
    private Boolean canEditSchool;
}
