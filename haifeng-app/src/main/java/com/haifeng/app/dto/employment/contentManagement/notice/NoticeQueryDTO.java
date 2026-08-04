package com.haifeng.app.dto.employment.contentManagement.notice;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告分页查询 DTO（用户端）
 * 字段与前端 NoticeQueryDTO 对齐：keyword 由前端同时映射为 title/summary/source 三个参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQueryDTO extends BasePageQueryDTO {

    @Size(max = 500, message = "标题长度不能超过500")
    private String title;

    @Size(max = 500, message = "摘要长度不能超过500")
    private String summary;

    @Size(max = 200, message = "来源长度不能超过200")
    private String source;

    @Size(max = 30, message = "公告类别长度不能超过30")
    private String noticeCategory;

    @Size(max = 50, message = "公告类型长度不能超过50")
    private String noticeType;

    @Size(max = 30, message = "省份长度不能超过30")
    private String province;

    @Size(max = 50, message = "城市长度不能超过50")
    private String city;

    @Size(max = 10, message = "年份长度不能超过10")
    private String year;
}
