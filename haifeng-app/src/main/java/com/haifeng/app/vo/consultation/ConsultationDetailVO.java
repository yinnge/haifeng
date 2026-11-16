package com.haifeng.app.vo.consultation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 教师端咨询留言详情。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String bizType;

    private String bizTypeDesc;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    private String bizName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderMemberId;

    private String message;

    private String wechat;

    private String phone;

    private String clientIp;

    private Integer status;

    private String statusDesc;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
