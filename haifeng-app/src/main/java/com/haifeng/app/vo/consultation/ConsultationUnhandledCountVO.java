package com.haifeng.app.vo.consultation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 教师端未处理留言数（角标）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationUnhandledCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer unhandledCount;
}
