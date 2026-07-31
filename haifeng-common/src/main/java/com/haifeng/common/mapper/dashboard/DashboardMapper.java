package com.haifeng.common.mapper.dashboard;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    @Select("SELECT DATE(created_at) AS date, COUNT(*) AS count " +
            "FROM t_member " +
            "WHERE created_at >= #{start} AND created_at < #{end} AND is_deleted = false " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY DATE(created_at)")
    List<Map<String, Object>> countMembersByDate(
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end);

    @Select("SELECT DATE(created_at) AS date, COUNT(*) AS count " +
            "FROM member_orders " +
            "WHERE created_at >= #{start} AND created_at < #{end} AND is_deleted = false " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY DATE(created_at)")
    List<Map<String, Object>> countOrdersByDate(
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end);
}
