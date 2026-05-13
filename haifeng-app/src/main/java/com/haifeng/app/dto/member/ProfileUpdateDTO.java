package com.haifeng.app.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDTO {

    @Size(max = 50, message = "真实姓名最多50个字符")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最多100个字符")
    private String email;

    private String gender;

    @Size(max = 100, message = "学校名称最多100个字符")
    private String schoolName;

    @Size(max = 30, message = "省份最多30个字符")
    private String province;

    @Size(max = 50, message = "城市最多50个字符")
    private String city;

    @Size(max = 100, message = "专业最多100个字符")
    private String major;

    private String identity;

    @Size(max = 20, message = "年级最多20个字符")
    private String grade;

    @Size(max = 20, message = "学历层次最多20个字符")
    private String educationLevel;
}
