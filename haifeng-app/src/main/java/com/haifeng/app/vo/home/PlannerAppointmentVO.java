package com.haifeng.app.vo.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * C 端规划师预约信息 VO：头像 + 姓名 + 联系方式。
 * 与详情接口拆分，避免联系方式出现在通用详情缓存中。
 */
@Data
public class PlannerAppointmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** svg 头像（本地静态资源相对路径，可空） */
    private String avatarUrl;

    /** 规划师姓名 */
    private String name;

    /** 邮箱（可空） */
    private String email;

    /** 手机号（可空，不唯一） */
    private String phone;

    /** 地址（可空） */
    private String address;

    /** 微信二维码 URL（可空） */
    private String wechatQrcodeUrl;
}
