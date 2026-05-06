package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.OrderQueryDTO;
import com.haifeng.admin.vo.user.OrderDetailVO;
import com.haifeng.admin.vo.user.OrderListVO;

public interface MemberOrderService {

    IPage<OrderListVO> page(OrderQueryDTO dto);

    OrderDetailVO detail(Long id);

    String getWechatPlaintext(Long id);

    void delete(Long id);
}
