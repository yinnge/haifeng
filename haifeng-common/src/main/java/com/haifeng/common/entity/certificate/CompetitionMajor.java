package com.haifeng.common.entity.certificate;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_competition_major")
public class CompetitionMajor {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long competitionId;

    private Long majorId;

    private String majorName;

    private String competitionName;

    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
