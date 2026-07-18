package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingsVO implements Serializable {

    private Integer ruanke;

    private Integer xiaoyouhui;

    private Integer wushulian;

    private Integer qs;

    private Integer usnews;
}
