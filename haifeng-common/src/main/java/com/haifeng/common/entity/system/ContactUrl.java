package com.haifeng.common.entity.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactUrl implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 微信二维码URL
     */
    private String wechat;

    /**
     * 微博主页URL
     */
    private String weibo;

    /**
     * 知乎主页URL
     */
    private String zhihu;

    /**
     * 抖音主页URL
     */
    private String douyin;

    /**
     * B站主页URL
     */
    private String bilibili;
}
