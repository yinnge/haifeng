package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端考研方向→本科专业列表 VO（接口2，对端是本科专业） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UndergraduateMajorDirectionBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String majorName;
}
