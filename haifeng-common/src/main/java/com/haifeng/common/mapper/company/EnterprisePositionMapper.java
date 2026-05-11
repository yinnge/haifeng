package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.EnterprisePosition;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterprisePositionMapper extends BaseMapper<EnterprisePosition> {

    @Delete("DELETE FROM t_enterprise_position WHERE enterprise_id = #{enterpriseId}")
    int deleteByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
}
