package com.haifeng.common.vo.algorithm.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionUniversityOptionVO {

    /** 大学 ID(雪花主键,前端调 group/page 时回传) */
    private Long id;

    /** 大学名 */
    private String name;

    /** 城市名(前端展示用) */
    private String cityName;

    /** 大学 tags 列表(前端展示用) */
    private List<String> tags;
}
