package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member_profile")
public class MemberProfile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;

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

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
