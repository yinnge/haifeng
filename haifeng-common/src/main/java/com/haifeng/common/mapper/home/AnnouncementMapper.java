package com.haifeng.common.mapper.home;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.home.Announcement;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    @Delete("DELETE FROM t_announcements WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
