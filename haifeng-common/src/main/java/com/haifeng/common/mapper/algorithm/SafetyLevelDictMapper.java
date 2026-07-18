package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SafetyLevelDictMapper extends BaseMapper<SafetyLevelDict> {

    @Select("SELECT * FROM t_safety_level_dict WHERE #{coefficient} >= min_coefficient AND #{coefficient} < max_coefficient AND is_deleted = FALSE LIMIT 1")
    SafetyLevelDict selectByCoefficient(@Param("coefficient") BigDecimal coefficient);

    @Select("SELECT * FROM t_safety_level_dict WHERE is_deleted = FALSE ORDER BY level ASC")
    java.util.List<SafetyLevelDict> selectAll();

    @Select("SELECT COUNT(*) FROM t_safety_level_dict WHERE code = #{code} AND is_deleted = FALSE")
    int countByCode(@Param("code") String code);

    @Select("SELECT COUNT(*) FROM t_safety_level_dict WHERE code = #{code} AND level != #{excludeLevel} AND is_deleted = FALSE")
    int countByCodeExclude(@Param("code") String code, @Param("excludeLevel") Short excludeLevel);

    @Select("SELECT * FROM t_safety_level_dict WHERE level = #{level} AND is_deleted = TRUE")
    SafetyLevelDict selectDeletedByLevel(@Param("level") Short level);

    @Update("<script>" +
            "UPDATE t_safety_level_dict SET is_deleted = TRUE WHERE level IN " +
            "<foreach collection='levels' item='level' open='(' separator=',' close=')'>" +
            "#{level}" +
            "</foreach>" +
            "</script>")
    int batchSoftDelete(@Param("levels") List<Short> levels);
}
