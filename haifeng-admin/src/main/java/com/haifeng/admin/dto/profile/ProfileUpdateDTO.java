package com.haifeng.admin.dto.profile;

import lombok.Data;

@Data
public class ProfileUpdateDTO {

    private String username;

    private String phone;

    private String email;

    private String avatar;
}
