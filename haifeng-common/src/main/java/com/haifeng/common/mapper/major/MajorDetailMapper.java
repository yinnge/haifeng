package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.MajorDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorDetailMapper extends BaseMapper<MajorDetail> {

    /**
     * 查询某专业的启用中详情。
     * 委托 MP 内置 selectOne，借助 MajorDetail 的 @TableName(autoResultMap = true)
     * 自动套用 StringArrayTypeHandler，确保 mainCourses / knowledgeSkills 等数组字段正确回显。
     */
    default MajorDetail selectByMajorId(@Param("majorId") Long majorId) {
        return selectOne(new LambdaQueryWrapper<MajorDetail>()
                .eq(MajorDetail::getMajorId, majorId)
                .eq(MajorDetail::getStatus, (short) 1));
    }

    @Select("SELECT COUNT(*) > 0 FROM t_major_detail WHERE major_id = #{majorId}")
    boolean existsByMajorId(@Param("majorId") Long majorId);
}
