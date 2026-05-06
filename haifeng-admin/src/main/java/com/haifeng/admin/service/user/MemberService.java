package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.MemberQueryDTO;
import com.haifeng.admin.dto.user.MemberStatusDTO;
import com.haifeng.admin.vo.user.MemberDetailVO;
import com.haifeng.admin.vo.user.MemberListVO;

public interface MemberService {

    /**
     * 分页查询用户列表
     */
    IPage<MemberListVO> page(MemberQueryDTO dto);

    /**
     * 获取用户详情
     */
    MemberDetailVO detail(Long id);

    /**
     * 修改用户状态
     */
    void updateStatus(Long id, MemberStatusDTO dto);

    /**
     * 获取用户微信明文（需记录操作日志）
     */
    String getWechatPlaintext(Long id);
}
