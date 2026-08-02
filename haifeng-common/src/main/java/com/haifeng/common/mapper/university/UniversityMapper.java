package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.University;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UniversityMapper extends BaseMapper<University> {

    @Select("SELECT id FROM t_universities WHERE name = #{name} AND status = 1 LIMIT 1")
    Long selectIdByName(@Param("name") String name);

    /**
     * 按名称查 id/name/cityName（只查这三列）。
     * 走 MP 内置 selectOne，避免自定义 @Select 丢失 typeHandler。
     */
    default University selectIdAndCityByName(String name) {
        return selectOne(new LambdaQueryWrapper<University>()
                .select(University::getId, University::getName, University::getCityName)
                .eq(University::getName, name)
                .eq(University::getStatus, 1)
                .last("LIMIT 1"));
    }

    /**
     * 按名称查完整记录（含 tags 数组列）。
     * 走 MP 内置 selectOne，autoResultMap 才会给 tags 套 StringListTypeHandler。
     */
    default University selectByName(String name) {
        return selectOne(new LambdaQueryWrapper<University>()
                .eq(University::getName, name)
                .eq(University::getStatus, 1)
                .last("LIMIT 1"));
    }
}
