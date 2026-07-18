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
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.user.ReferralCommissionMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberOrderServiceImpl implements MemberOrderService {

    private final MemberOrderMapper memberOrderMapper;
    private final ReferralCommissionMapper commissionMapper;
    private final MemberMapper memberMapper;
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
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
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
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return order.getWechatId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }

        order.setDeleted(true);
        order.setUpdatedAt(OffsetDateTime.now());
        int affected = memberOrderMapper.updateById(order);
        if (affected == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }

        log.info("删除订单成功: orderId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        MemberOrder order = memberOrderMapper.selectByIdIgnoreDeleted(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }

        // 检查是否有佣金记录引用此订单
        Long commissionCount = commissionMapper.selectCount(
                new LambdaQueryWrapper<com.haifeng.common.entity.user.ReferralCommission>()
                        .eq(com.haifeng.common.entity.user.ReferralCommission::getOrderId, id)
                        .eq(com.haifeng.common.entity.user.ReferralCommission::getDeleted, false));
        if (commissionCount > 0) {
            throw new BusinessException(400, "该订单存在关联的佣金记录，无法硬删除");
        }

        memberOrderMapper.hardDeleteById(id);
        log.info("硬删除订单成功: orderId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        MemberOrder order = memberOrderMapper.selectByIdIgnoreDeleted(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }

        if (!order.getDeleted()) {
            throw new BusinessException(400, "该订单未被禁用，无需恢复");
        }

        // 校验订单所属会员是否仍有效
        com.haifeng.common.entity.user.Member member = memberMapper.selectById(order.getMemberId());
        if (member == null || member.getDeleted()) {
            throw new BusinessException(400, "该订单所属会员已不存在，无法恢复");
        }

        int affected = memberOrderMapper.restoreById(id, OffsetDateTime.now());
        if (affected == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }

        log.info("恢复订单成功: orderId={}", id);
    }
}
