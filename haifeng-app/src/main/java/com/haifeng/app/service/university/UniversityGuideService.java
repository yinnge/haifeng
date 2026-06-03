package com.haifeng.app.service.university;

import com.haifeng.app.vo.university.*;

public interface UniversityGuideService {

    /** 概览：自定义标签 + 联院校简要字段 */
    UniversityGuideOverviewVO overview(Long universityId);

    /** 基础生存类 */
    UniversityGuideSurvivalVO survival(Long universityId);

    /** 学业规划类（需 Pro） */
    UniversityGuideAcademicVO academic(Long universityId);

    /** 社交融入类 */
    UniversityGuideSocialVO social(Long universityId);

    /** 权益与安全类 */
    UniversityGuideSafetyVO safety(Long universityId);

    /** 周边生活类 */
    UniversityGuideLifeVO life(Long universityId);
}
