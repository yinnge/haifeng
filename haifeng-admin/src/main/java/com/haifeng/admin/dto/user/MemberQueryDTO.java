package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberQueryDTO extends BasePageQueryDTO {

    /**
     * 手机号（模糊查询）
     */
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
}
