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
 * 教师端咨询留言列表项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * planner / institution
     */
    private String bizType;

    private String bizTypeDesc;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    /**
     * 提交时的业务名称快照
     */
    private String bizName;

    private String message;

    private String wechat;

    private String phone;

    /**
     * 0-待处理 1-已处理
     */
    private Integer status;

    private String statusDesc;

    private OffsetDateTime createdAt;
}
