package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @Select("SELECT * FROM t_safety_level_dict WHERE level = #{level}")
    SafetyLevelDict selectByIdCustom(@Param("level") Short level);

    @Select("SELECT * FROM t_safety_level_dict WHERE level = #{level} AND is_deleted = TRUE")
    SafetyLevelDict selectDeletedByLevel(@Param("level") Short level);

    @Select("<script>" +
            "SELECT * FROM t_safety_level_dict " +
            "<where>" +
            "<if test='params.isDeleted != null'>AND is_deleted = #{params.isDeleted}</if>" +
            "</where>" +
            "ORDER BY level ASC" +
            "</script>")
    IPage<SafetyLevelDict> selectPageCustom(Page<?> page, @Param("params") Map<String, Object> params);

    @Update("UPDATE t_safety_level_dict SET is_deleted = #{isDeleted}, version = version + 1, updated_at = NOW() WHERE level = #{level}")
    int updateStatus(@Param("level") Short level, @Param("isDeleted") Boolean isDeleted);

    @Update("<script>" +
            "UPDATE t_safety_level_dict SET code = #{e.code}, name = #{e.name}, name_short = #{e.nameShort}, " +
            "min_coefficient = #{e.minCoefficient}, max_coefficient = #{e.maxCoefficient}, " +
            "color = #{e.color}, confidence = #{e.confidence}, " +
            "confidence_reason = #{e.confidenceReason}, description = #{e.description}, " +
            "is_deleted = FALSE, version = 0, updated_at = NOW() " +
            "WHERE level = #{e.level}" +
            "</script>")
    int restoreDeleted(@Param("e") SafetyLevelDict entity);

    @Update("<script>" +
            "UPDATE t_safety_level_dict SET is_deleted = TRUE WHERE level IN " +
            "<foreach collection='levels' item='level' open='(' separator=',' close=')'>" +
            "#{level}" +
            "</foreach>" +
            "</script>")
    int batchSoftDelete(@Param("levels") List<Short> levels);
}
