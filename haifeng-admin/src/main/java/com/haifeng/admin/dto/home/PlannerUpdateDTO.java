package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.home.PlannerServiceItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PlannerUpdateDTO {

    /** 关联用户 ID（t_member.id，逻辑外键） */
    @NotNull(message = "关联用户ID不能为空")
    private Long userId;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名最长64字符")
    private String name;

    /** 服务对象：高考 / 考研 */
    @NotBlank(message = "服务对象不能为空")
    @Size(max = 32, message = "服务对象最长32字符")
    private String serviceTarget;

    @Size(max = 64, message = "职位最长64字符")
    private String position;

    /** 所属省份（ProvinceEnum 裸值） */
    @Size(max = 30, message = "省份最长30字符")
    private String province;

    @Size(max = 512, message = "列表描述最长512字符")
    private String listDesc;

    private List<String> listTags;

    /** svg 头像相对路径（可空） */
    @Size(max = 512, message = "头像路径最长512字符")
    private String avatarUrl;

    @Size(max = 32, message = "从业年限最长32字符")
    private String workYears;

    @Size(max = 32, message = "服务考生最长32字符")
    private String servedCount;

    @Size(max = 32, message = "初稿时效最长32字符")
    private String draftSla;

    @Size(max = 256, message = "认证描述最长256字符")
    private String certDesc;

    private String description;

    private List<String> detailTags;

    @Valid
    private List<PlannerServiceItem> services;

    /** 邮箱（可空） */
    @Size(max = 128, message = "邮箱最长128字符")
    private String email;

    /** 手机号（可空，不唯一） */
    @Size(max = 20, message = "手机号最长20字符")
    private String phone;

    /** 地址（可空） */
    @Size(max = 256, message = "地址最长256字符")
    private String address;

    /** 微信二维码相对路径（可空） */
    @Size(max = 512, message = "微信二维码路径最长512字符")
    private String wechatQrcodeUrl;

    private Integer sortOrder;
}
