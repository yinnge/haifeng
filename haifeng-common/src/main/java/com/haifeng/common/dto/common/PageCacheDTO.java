package com.haifeng.common.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果缓存中转结构
 *
 * <p>MyBatis-Plus 的 {@link com.baomidou.mybatisplus.extension.plugins.pagination.Page}
 * 通过 Jackson 序列化进 Redis 时容易丢失类型，借助本类做一次扁平化转换。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageCacheDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private long current;
    private long size;
}
