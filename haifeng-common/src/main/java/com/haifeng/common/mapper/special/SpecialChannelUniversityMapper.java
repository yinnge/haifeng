package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SpecialChannelUniversityMapper extends BaseMapper<SpecialChannelUniversity> {

    @Select("SELECT COUNT(*) FROM t_special_channel_university WHERE channel_code = #{channelCode} AND university_id = #{universityId} AND year = #{year}")
    int countByUnique(@Param("channelCode") String channelCode, @Param("universityId") Long universityId, @Param("year") Short year);

    @Select("SELECT COUNT(*) FROM t_special_channel_university WHERE channel_code = #{channelCode} AND university_id = #{universityId} AND year = #{year} AND id != #{excludeId}")
    int countByUniqueExclude(@Param("channelCode") String channelCode, @Param("universityId") Long universityId, @Param("year") Short year, @Param("excludeId") Long excludeId);
}
