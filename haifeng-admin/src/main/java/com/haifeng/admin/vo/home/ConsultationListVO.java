package com.haifeng.admin.vo.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Admin 端咨询留言列表项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String bizType;

    private String bizTypeDesc;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    private String bizName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long teacherMemberId;

    /**
     * 接收教师用户名（联查 t_member）
     */
    private String teacherUsername;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderMemberId;

    private String message;

    private String wechat;

    private String phone;

    private Integer status;

    private String statusDesc;

    private OffsetDateTime createdAt;
}
