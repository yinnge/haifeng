package com.haifeng.app.vo.home;

import lombok.Data;

import java.io.Serializable;

@Data
public class AnnouncementDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    private String tag;
}
