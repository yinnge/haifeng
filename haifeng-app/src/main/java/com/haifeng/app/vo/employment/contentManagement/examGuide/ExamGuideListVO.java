package com.haifeng.app.vo.employment.contentManagement.examGuide;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamGuideListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String guideCategory;

    private String guideType;

    private String title;

    private String subtitle;

    private String[] tags;

    private String authorName;

    private String authorTitle;
}
