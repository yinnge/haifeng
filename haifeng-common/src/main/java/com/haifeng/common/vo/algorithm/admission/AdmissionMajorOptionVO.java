package com.haifeng.common.vo.algorithm.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionMajorOptionVO {

    /**
     * t_major.id(雪花主键)
     * ⚠️ 仅用于前端展示/去重,不要回传给 group/page
     */
    private Long id;

    /**
     * t_admission_major_score.major_code(招生专业代码)
     * ✅ 前端调用 group/page 时使用这个字段过滤
     */
    private String majorCode;

    private String majorName;
    private String majorCategory;
    private String majorTags;
}
