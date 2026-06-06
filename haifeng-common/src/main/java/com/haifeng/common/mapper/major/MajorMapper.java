package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MajorMapper extends BaseMapper<Major> {

    @Select("SELECT id FROM t_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    Long selectIdByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT COUNT(*) > 0 FROM t_major WHERE major_code = #{majorCode}")
    boolean existsByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT * FROM t_major WHERE major_name = #{majorName} AND status = 1 LIMIT 1")
    Major findByMajorName(@Param("majorName") String majorName);

    @Select("SELECT major_code FROM t_major WHERE major_name = #{majorName} AND status = 1 LIMIT 1")
    String selectCodeByName(@Param("majorName") String majorName);

    /**
     * 按 major_category 分组统计专业数量（仅 status=1）
     * Service 层负责转换为 List<MajorCategoryStatVO>
     */
    @Select("SELECT major_category AS majorCategory, COUNT(*) AS count " +
            "FROM t_major " +
            "WHERE status = 1 AND major_category IS NOT NULL " +
            "GROUP BY major_category " +
            "ORDER BY COUNT(*) DESC")
    List<Map<String, Object>> countByCategory();
}
