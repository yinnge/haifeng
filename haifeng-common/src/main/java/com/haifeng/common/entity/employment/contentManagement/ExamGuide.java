package com.haifeng.common.entity.employment.contentManagement;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "t_exam_guide", autoResultMap = true)
public class ExamGuide implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private String guideCategory;

    private String guideType;

    private String title;

    private String subtitle;

    private String coverImage;

    private String iconClass;

    private String summary;

    private String content;

    private String[] tags;

    private String difficultyLevel;

    private String targetAudience;

    private String authorName;

    private String authorTitle;

    private Boolean isTop;

    private Boolean isRecommended;

    private Integer sortOrder;

    private Integer viewCount;

    private Integer likeCount;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
