package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.6 周边生活类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideLifeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> lifeServices;
}
