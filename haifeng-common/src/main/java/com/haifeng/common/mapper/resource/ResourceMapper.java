package com.haifeng.common.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.resource.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Update("UPDATE t_resource SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = false")
    int incrementViewCount(@Param("id") Long id);
}
