package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberQueryDTO extends BasePageQueryDTO {

    /**
     * 手机号（模糊查询）
     */
    @Size(max = 50, message = "手机号长度不能超过50")
    private String phone;

    /**
     * 会员类型：normal/pro/vip
     */
    private String memberType;

    /**
     * 微信号（等值查询，后端转换为盲索引）
     */
    private String wechatId;

    /**
     * 账号状态：active/disabled
     */
    private String status;

    /**
     * 邀请码（模糊查询）
     */
    @Size(max = 50, message = "邀请码长度不能超过50")
    private String inviteCode;
}
