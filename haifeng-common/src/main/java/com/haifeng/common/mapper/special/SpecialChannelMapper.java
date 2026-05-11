package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.SpecialChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SpecialChannelMapper extends BaseMapper<SpecialChannel> {

    @Select("SELECT COUNT(*) FROM t_special_channel WHERE channel_code = #{code}")
    int countByCode(@Param("code") String code);

    @Select("SELECT COUNT(*) FROM t_special_channel WHERE channel_name = #{name}")
    int countByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM t_special_channel WHERE channel_name = #{name} AND id != #{excludeId}")
    int countByNameExclude(@Param("name") String name, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM t_special_channel WHERE channel_code = #{code} AND id != #{excludeId}")
    int countByCodeExclude(@Param("code") String code, @Param("excludeId") Long excludeId);
}
