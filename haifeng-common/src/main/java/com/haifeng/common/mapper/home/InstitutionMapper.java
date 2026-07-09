package com.haifeng.common.mapper.home;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.home.Institution;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InstitutionMapper extends BaseMapper<Institution> {

    @Delete("DELETE FROM t_institutions WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
