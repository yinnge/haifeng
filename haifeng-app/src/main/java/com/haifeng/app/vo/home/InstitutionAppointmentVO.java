package com.haifeng.app.vo.home;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * C 端培训机构预约信息 VO：机构名 + 联系方式。
 * <p>与详情接口拆分，联系方式不进通用详情缓存。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstitutionAppointmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 机构名称
     */
    private String name;

    /**
     * 地址（可空）
     */
    private String address;

    /**
     * 微信二维码 URL（可空）
     */
    private String wechatQrcodeUrl;

    /**
     * 邮箱（可空）
     */
    private String email;

    /**
     * 联系电话（可空）
     */
    private String phone;
}
