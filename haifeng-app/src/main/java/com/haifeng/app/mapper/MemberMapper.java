package com.haifeng.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.app.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员 Mapper
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
