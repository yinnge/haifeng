package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端实验室列表 VO（spec §3.1） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String labType;
}
