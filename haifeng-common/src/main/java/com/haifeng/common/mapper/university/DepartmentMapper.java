package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_department WHERE university_id = #{universityId} AND department_name = #{departmentName} AND status = 1)")
    boolean existsByUniversityIdAndName(@Param("universityId") Long universityId, @Param("departmentName") String departmentName);
}
