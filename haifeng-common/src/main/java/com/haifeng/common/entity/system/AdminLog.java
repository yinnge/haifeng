package com.haifeng.common.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_logs")
public class AdminLog {

    @TableId(type = IdType.ASSIGN_ID)
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
