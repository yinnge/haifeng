package com.haifeng.admin.vo.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String adminName;
    private String operation;
    private String requestMethod;
    private String result;
    private String ip;
    private OffsetDateTime createdAt;
}
