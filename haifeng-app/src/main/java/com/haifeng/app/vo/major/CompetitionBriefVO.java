package com.haifeng.app.vo.major;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端专业→竞赛简明 VO（spec 任务3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 竞赛雪花 ID：序列化为字符串，前端据此跳转 /competition/{competitionId}，不可丢精度 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long competitionId;

    private String competitionName;
}
