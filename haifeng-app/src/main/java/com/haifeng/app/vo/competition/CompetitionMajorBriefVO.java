package com.haifeng.app.vo.competition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端竞赛→专业简明 VO（spec 任务2接口3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionMajorBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 专业雪花 ID：序列化为字符串，前端据此跳转 /major/{majorId}，不可丢精度 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long majorId;

    private String majorName;
}
