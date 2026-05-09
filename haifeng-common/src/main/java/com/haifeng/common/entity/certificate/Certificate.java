package com.haifeng.common.entity.certificate;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_certificate", autoResultMap = true)
public class Certificate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String certName;

    private String category;

    private String certLevel;

    private String applicableMajor;

    private String registrationTime;

    private String examTime;

    private Integer examFee;

    private String certIntro;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> examRequirements;

    private String examArrangement;

    private String officialWebsite;

    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
