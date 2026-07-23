package com.haifeng.admin.vo.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Long adminId;
    private String adminName;
    private String operation;
    private String requestPath;
    private String requestMethod;
    private String requestParams;
    private String result;
    private String errorMsg;
    private String ip;
    private OffsetDateTime createdAt;
}
