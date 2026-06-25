package com.haifeng.app.vo.algorithm.wish;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class WishPlanListVO {
    private Integer id;
    private String planName;
    private Short planYear;
    private String planProvince;
    private String reformModel;
    private String planBatch;
    private Integer userScore;
    private Integer userRank;
    private Integer boLimit;
    private Integer chongLimit;
    private Integer wenLimit;
    private Integer baoLimit;
    private Integer dieLimit;
    private OffsetDateTime createdAt;
}
