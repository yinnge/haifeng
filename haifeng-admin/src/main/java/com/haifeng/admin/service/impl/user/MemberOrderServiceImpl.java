package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.OrderQueryDTO;
import com.haifeng.admin.service.user.MemberOrderService;
import com.haifeng.admin.vo.user.OrderDetailVO;
import com.haifeng.admin.vo.user.OrderListVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberOrderServiceImpl implements MemberOrderService {

    private final MemberOrderMapper memberOrderMapper;
    private final SecurityProperties securityProperties;

    @Override
    public IPage<OrderListVO> page(OrderQueryDTO dto) {
        Page<MemberOrder> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<MemberOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberOrder::getDeleted, false);

        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(MemberOrder::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getWechatId())) {
            String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
            wrapper.eq(MemberOrder::getWechatIdIndex, blindIndex);
        }
        if (StringUtils.hasText(dto.getOperatorName())) {
            wrapper.like(MemberOrder::getOperatorName, dto.getOperatorName());
        }
        if (StringUtils.hasText(dto.getOrderType())) {
            wrapper.eq(MemberOrder::getOrderType, dto.getOrderType());
        }

        wrapper.orderByDesc(MemberOrder::getCreatedAt);

        IPage<MemberOrder> orderPage = memberOrderMapper.selectPage(page, wrapper);

        return orderPage.convert(order -> {
            OrderListVO vo = new OrderListVO();
            BeanUtils.copyProperties(order, vo);
            vo.setWechatId(DesensitizeUtil.desensitizeWechat(order.getWechatId()));
            if (order.getOrderType() != null) {
                vo.setOrderType(order.getOrderType().getValue());
            }
            if (order.getBeforeType() != null) {
                vo.setBeforeType(order.getBeforeType().getValue());
            }
            if (order.getAfterType() != null) {
                vo.setAfterType(order.getAfterType().getValue());
            }
            return vo;
        });
    }

    @Override
    public OrderDetailVO detail(Long id) {
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(404, "订单不存在");
        }

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setWechatId(DesensitizeUtil.desensitizeWechat(order.getWechatId()));
        if (order.getOrderType() != null) {
            vo.setOrderType(order.getOrderType().getValue());
        }
        if (order.getBeforeType() != null) {
            vo.setBeforeType(order.getBeforeType().getValue());
        }
        if (order.getAfterType() != null) {
            vo.setAfterType(order.getAfterType().getValue());
        }
        return vo;
    }

    @Override
    public String getWechatPlaintext(Long id) {
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(404, "订单不存在");
        }
        return order.getWechatId();
    }

    @Override
    public void delete(Long id) {
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(404, "订单不存在");
        }

        order.setDeleted(true);
        order.setUpdatedAt(OffsetDateTime.now());
        memberOrderMapper.updateById(order);

        log.info("删除订单成功: orderId={}", id);
    }

    @Override
    public void hardDelete(Long id) {
        MemberOrder order = memberOrderMapper.selectByIdIgnoreDeleted(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        memberOrderMapper.hardDeleteById(id);
        log.info("硬删除订单成功: orderId={}", id);
    }

    @Override
    public void restore(Long id) {
        MemberOrder order = memberOrderMapper.selectByIdIgnoreDeleted(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (!order.getDeleted()) {
            throw new BusinessException(400, "该订单未被禁用，无需恢复");
        }

        memberOrderMapper.restoreById(id, OffsetDateTime.now());
        log.info("恢复订单成功: orderId={}", id);
    }
}
