package com.haifeng.common.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BasePageQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 10, message = "每页最小10条")
    @Max(value = 100, message = "每页最大100条")
    private Integer size = 10;

    /**
     * 可选：按指定 ID 集合过滤（如「仅返回我收藏的院校」）。
     * 由各列表 page() 自行 AND 进查询；空集合视作「无匹配」直接返回空页，避免生成 IN () 非法 SQL。
     * 放在基类，供大学/专业/城市等所有收藏类型列表复用。
     */
    @Size(max = 200, message = "ID集合最多200个")
    private List<Long> ids;
}
