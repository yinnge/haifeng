package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户高考档案 Mapper
 */
@Mapper
public interface MemberGaokaoMapper extends BaseMapper<MemberGaokao> {
    @Select("SELECT * FROM t_member_gaokao WHERE member_id = #{memberId}")
    MemberGaokao selectByMemberId(@Param("memberId") Long memberId);
}
