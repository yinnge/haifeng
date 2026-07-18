package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.MajorConstraint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface MajorConstraintMapper extends BaseMapper<MajorConstraint> {
    @Select("SELECT COUNT(*) FROM t_major_constraint WHERE major_code = #{majorCode} AND constraint_code = #{constraintCode} AND is_deleted = FALSE")
    int countByBusinessKey(@Param("majorCode") String majorCode, @Param("constraintCode") String constraintCode);

    @Select("SELECT * FROM t_major_constraint WHERE major_code = #{majorCode} AND constraint_code = #{constraintCode} AND is_deleted = TRUE LIMIT 1")
    MajorConstraint selectDeletedByBusinessKey(@Param("majorCode") String majorCode, @Param("constraintCode") String constraintCode);

    @Select("<script>" +
            "SELECT CONCAT(major_code, '_', constraint_code) AS business_key " +
            "FROM t_major_constraint " +
            "WHERE is_deleted = FALSE AND " +
            "<foreach collection='keys' item='k' open='(' separator=') OR (' close=')'>" +
            "major_code = #{k.majorCode} AND constraint_code = #{k.constraintCode}" +
            "</foreach>" +
            "</script>")
    List<String> selectExistingKeys(@Param("keys") List<Map<String, Object>> keys);

    @Select({"<script>",
            "SELECT * FROM t_major_constraint",
            "WHERE is_deleted = TRUE AND (",
            "<foreach collection='keys' item='k' separator=' OR '>",
            "(major_code = #{k.majorCode} AND constraint_code = #{k.constraintCode})",
            "</foreach>",
            ")</script>"})
    List<MajorConstraint> selectDeletedByKeys(@Param("keys") List<Map<String, Object>> keys);

    @Update("<script>" +
            "UPDATE t_major_constraint SET is_deleted = TRUE WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids);
}
