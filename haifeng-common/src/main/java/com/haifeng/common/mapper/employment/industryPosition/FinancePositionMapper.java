package com.haifeng.common.mapper.employment.industryPosition;

import com.haifeng.common.entity.employment.industryPosition.FinancePosition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface FinancePositionMapper extends BaseMapper<FinancePosition> {

    /**
     * 物理删除（真实 DELETE FROM，绕过 MyBatis-Plus 逻辑删除拦截）
     */
    @Delete("DELETE FROM t_finance_position WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("<script>DELETE FROM t_finance_position WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int physicalDeleteBatchIds(@Param("ids") Collection<Long> ids);

    /**
     * 查询岗位类别的去重值列表（仅未删除的记录）
     */
    @Select("SELECT DISTINCT position_category FROM t_finance_position WHERE position_category IS NOT NULL AND position_category != '' AND is_deleted = false ORDER BY position_category")
    List<String> selectDistinctPositionCategories();
}
