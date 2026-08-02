package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.GaokaoConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GaokaoConfigMapper extends BaseMapper<GaokaoConfig> {

    /**
     * 查询全局配置单例。
     * 必须走 MP 内置 selectOne（autoResultMap 才能套上 BigDecimalListTypeHandler），
     * 自定义 @Select 不套 typeHandler，year_weights 会反序列化为 null。
     */
    default GaokaoConfig selectSingleton() {
        return selectOne(new LambdaQueryWrapper<GaokaoConfig>().eq(GaokaoConfig::getId, 1));
    }
}
