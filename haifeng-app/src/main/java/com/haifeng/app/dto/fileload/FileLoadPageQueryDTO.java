package com.haifeng.app.dto.fileload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文件资料（高考/中考/考公/考研/大学等）C 端列表分页 DTO。
 * <p>与全局 BasePageQueryDTO 解耦：默认每页 12 条，允许档位 12/24/36/58/110；
 * 因 110 超出 BasePageQueryDTO 的 @Max(100) 全局契约，故独立定义，不影响其它分页接口。
 */
@Data
public class FileLoadPageQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 12, message = "每页最小12条")
    @Max(value = 110, message = "每页最大110条")
    private Integer size = 12;

    /**
     * 独立搜索关键词（与 subject/applicableStage/tag 三个筛选项正交，可单独用也可叠加）。
     * 命中 file_name 或 description（PostgreSQL ILIKE 不区分大小写包含），空值表示不过滤。
     */
    @Size(max = 50, message = "搜索关键词长度不能超过50")
    private String keyword;

    /**
     * 收藏过滤：传入"已收藏的文件 id 集合"时，仅返回命中项，与原筛选（subject/stage/tag/keyword）以 AND 叠加。
     * 空集合表示"无收藏" → page() 内直接返回空页，避免 IN () 生成非法 SQL。
     * 不传（null）则不生效。各文件模块（college/high/middle/civil/graduate/postgrad-exam 含 teacher）共用此字段。
     */
    @Size(max = 200, message = "ID集合最多200个")
    private List<Long> ids;
}
