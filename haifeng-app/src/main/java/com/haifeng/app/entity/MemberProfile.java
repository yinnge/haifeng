package com.haifeng.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 会员资料实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member_profile")
public class MemberProfile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 身份（如：北京大学）
     */
    private String identity;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级（大三/研一）
     */
    private String grade;

    /**
     * 学历层次（大学生/研究生）
     */
    private String educationLevel;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
