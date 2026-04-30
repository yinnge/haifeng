package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
