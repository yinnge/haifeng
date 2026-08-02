package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.DepartmentReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentReportMapper extends BaseMapper<DepartmentReport> {

    /**
     * 按院系ID查询启用的专业报告。
     * 必须走 MP 内置 selectOne（autoResultMap 才能套上 JsonbTypeHandler），
     * 自定义 @Select 不套 typeHandler，10 个 JSONB 字段会反序列化为 null。
     */
    default DepartmentReport selectByDepartmentId(Long departmentId) {
        return selectOne(new LambdaQueryWrapper<DepartmentReport>()
                .eq(DepartmentReport::getDepartmentId, departmentId)
                .eq(DepartmentReport::getStatus, 1)
                .last("LIMIT 1"));
    }
}
