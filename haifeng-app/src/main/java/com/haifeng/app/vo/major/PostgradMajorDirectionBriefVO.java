package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端本科专业→考研方向列表 VO（接口1，对端是考研专业） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgradMajorDirectionBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String postgradMajorName;
}
