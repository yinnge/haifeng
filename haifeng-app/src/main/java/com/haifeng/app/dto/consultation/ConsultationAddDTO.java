package com.haifeng.app.dto.consultation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * C 端提交咨询留言。
 */
@Data
public class ConsultationAddDTO {

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言最长1000字")
    private String message;

    /**
     * 微信号（可选）
     */
    @Size(max = 64, message = "微信号最长64字符")
    private String wechat;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
