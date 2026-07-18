package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ConstraintDictMapper extends BaseMapper<ConstraintDict> {
    @Select("SELECT COUNT(*) FROM t_constraint_dict WHERE name = #{name} AND is_deleted = FALSE")
    int countByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM t_constraint_dict WHERE name = #{name} AND code != #{excludeCode} AND is_deleted = FALSE")
    int countByNameExclude(@Param("name") String name, @Param("excludeCode") String excludeCode);

    @Select("SELECT code FROM t_constraint_dict WHERE name = #{name} AND is_deleted = FALSE")
    String selectCodeByName(@Param("name") String name);

    @Select("SELECT * FROM t_constraint_dict WHERE is_active = true AND is_deleted = FALSE ORDER BY sort_order ASC")
    List<ConstraintDict> selectActiveList();

    @Select("<script>" +
            "SELECT code, severity FROM t_constraint_dict " +
            "WHERE code IN " +
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " AND is_active = true AND is_deleted = FALSE" +
            "</script>")
    List<ConstraintDict> selectSeverityByCodes(@Param("codes") List<String> codes);

    @Select("SELECT * FROM t_constraint_dict WHERE code = #{code} AND is_deleted = TRUE")
    ConstraintDict selectDeletedByCode(@Param("code") String code);

    @Select("<script>" +
            "SELECT * FROM t_constraint_dict WHERE is_deleted = FALSE AND code IN " +
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            "</script>")
    List<ConstraintDict> selectActiveBatchIds(@Param("codes") List<String> codes);

    @Update("<script>" +
            "UPDATE t_constraint_dict SET is_deleted = TRUE WHERE code IN " +
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            "</script>")
    int batchSoftDelete(@Param("codes") List<String> codes);
}
