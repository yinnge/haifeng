package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * C 端院校列表查询 DTO
 * 全部筛选字段 optional；name 走 LIKE，provinceName 走「剥后缀归一化」匹配（兼容简称/全称），
 * tags 走 text[] 包含匹配（多标签 AND 交集），channelCode 走特殊招生通道关联表匹配
 * （统招=前端不传参，即查全部院校）；其余精准匹配（AND 组合）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityQueryDTO extends BasePageQueryDTO {

    /** 院校名称模糊（LIKE %name%） */
    @Size(max = 50, message = "院校名称长度不能超过50")
    private String name;

    @Size(max = 20, message = "省份名称长度不能超过20")
    private String provinceName;

    @Size(max = 20, message = "办学性质长度不能超过20")
    private String nature;

    @Size(max = 20, message = "院校类型长度不能超过20")
    private String category;

    @Size(max = 20, message = "学历层次长度不能超过20")
    private String educationLevel;

    /** 标签筛选（如 985/211/双一流），支持逗号分隔多值（?tags=985,211），多标签 AND 语义 */
    @Size(max = 10, message = "标签数量不能超过10")
    private List<String> tags;

    /** 招生通道编码（t_special_channel.channel_code）；统招不传，非统招经关联表过滤 */
    @Size(max = 50, message = "招生通道编码长度不能超过50")
    private String channelCode;
}
