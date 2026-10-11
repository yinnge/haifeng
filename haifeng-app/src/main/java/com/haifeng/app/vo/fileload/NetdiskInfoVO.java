package com.haifeng.app.vo.fileload;

import lombok.Data;

/**
 * 百度网盘取码信息（仅 VIP 用户经 /{id}/netdisk 获取；不含在列表/详情中，防止登录即绕过 VIP 门槛）
 */
@Data
public class NetdiskInfoVO {

    /** 百度网盘分享链接 */
    private String netdiskUrl;

    /** 提取码（可空，无提取码时前端不展示） */
    private String accessCode;
}
