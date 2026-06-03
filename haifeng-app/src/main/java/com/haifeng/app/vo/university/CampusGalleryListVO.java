package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 任务 4 校园图册列表 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusGalleryListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String imageType;
    private String imageUrl;
}
