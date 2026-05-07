package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.DepartmentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DepartmentReportMapper extends BaseMapper<DepartmentReport> {

    @Select("SELECT * FROM department_reports WHERE department_id = #{departmentId} AND status = 1 LIMIT 1")
    DepartmentReport selectByDepartmentId(@Param("departmentId") Long departmentId);

    @Select("SELECT EXISTS(SELECT 1 FROM department_reports WHERE department_id = #{departmentId})")
    boolean existsByDepartmentId(@Param("departmentId") Long departmentId);
}
