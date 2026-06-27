package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.GaokaoConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GaokaoConfigMapper extends BaseMapper<GaokaoConfig> {

    @Select("SELECT * FROM gaokao_config WHERE id = 1")
    GaokaoConfig selectSingleton();
}
