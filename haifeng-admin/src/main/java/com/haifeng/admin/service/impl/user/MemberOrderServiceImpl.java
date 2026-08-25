package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.OrderCreateDTO;
import com.haifeng.admin.dto.user.OrderQueryDTO;
import com.haifeng.admin.service.user.MemberOrderService;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.vo.user.OrderDetailVO;
import com.haifeng.admin.vo.user.OrderListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.entity.user.ReferralCommission;
import com.haifeng.common.enums.MemberType;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.enums.OrderStatus;
import com.haifeng.common.enums.OrderType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.user.ReferralCommissionMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.security.AuthUser;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberOrderServiceImpl implements MemberOrderService {

    private final MemberOrderMapper memberOrderMapper;
    private final ReferralCommissionMapper commissionMapper;
    private final MemberMapper memberMapper;
    private final SystemSettingsMapper settingsMapper;
    private final NotificationService notificationService;
    private final SecurityProperties securityProperties;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    // ==================== 查询方法 ====================

    @Override
    public IPage<OrderListVO> page(OrderQueryDTO dto) {
        return pageNormal(dto);
    }

    private IPage<OrderListVO> pageNormal(OrderQueryDTO dto) {
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
        if (StringUtils.hasText(dto.getOrderStatus())) {
            wrapper.eq(MemberOrder::getStatus, dto.getOrderStatus());
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
            if (order.getStatus() != null) {
                vo.setStatus(order.getStatus().getValue());
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
        if (order.getStatus() != null) {
            vo.setStatus(order.getStatus().getValue());
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

    // ==================== 订单生命周期方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO dto) {
        // 1. 校验用户存在
        Member member = memberMapper.selectById(dto.getMemberId());
        if (member == null || member.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 2. 校验目标类型合法性（vip未过期不能降级为pro）
        MemberType currentType = MemberType.fromValue(member.getMemberType());
        MemberType targetType = MemberType.fromValue(dto.getTargetType());

        if (currentType == MemberType.VIP && targetType == MemberType.PRO) {
            if (member.getExpireAt() != null && member.getExpireAt().isAfter(OffsetDateTime.now())) {
                throw new BusinessException(400, "VIP会员未过期，不能降级为Pro");
            }
        }

        // 3. 计算到期时间
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime beforeExpireAt = member.getExpireAt();
        OffsetDateTime newExpireAt;

        boolean isExpired = beforeExpireAt == null || beforeExpireAt.isBefore(now);
        if (isExpired) {
            newExpireAt = now.plusMonths(dto.getDurationMonths());
        } else {
            newExpireAt = beforeExpireAt.plusMonths(dto.getDurationMonths());
        }

        // 4. 判断订单类型
        boolean isTypeChange = currentType != targetType;
        OrderType orderType;
        if (currentType == MemberType.NORMAL || isTypeChange) {
            orderType = OrderType.NEW;
        } else {
            orderType = OrderType.RENEWAL;
        }

        // 5. 幂等检查：同一用户5分钟内不可重复创建同类型+同目标的订单
        Long duplicateCount = memberOrderMapper.selectCount(
                new LambdaQueryWrapper<MemberOrder>()
                        .eq(MemberOrder::getMemberId, dto.getMemberId())
                        .eq(MemberOrder::getOrderType, orderType)
                        .eq(MemberOrder::getAfterType, targetType)
                        .eq(MemberOrder::getDeleted, false)
                        .ge(MemberOrder::getCreatedAt, now.minusMinutes(5)));
        if (duplicateCount > 0) {
            throw new BusinessException(400, "操作过于频繁，请5分钟后再试");
        }

        // 6. 查询系统设置，计算金额
        SystemSettings settings = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));
        if (settings == null) {
            throw new BusinessException(500, "系统设置不存在，无法处理");
        }

        BigDecimal finalAmount = dto.getAmount();
        if (finalAmount == null) {
            finalAmount = calculateAmount(settings, dto.getTargetType(), dto.getDurationMonths());
        }

        // 7. 创建订单记录，status = PENDING（不更新用户表、不处理佣金、不发通知）
        Long orderId = SnowflakeIdGenerator.nextId();
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        String operatorName = currentUser != null ? currentUser.getUsername() : null;

        MemberOrder order = MemberOrder.builder()
                .id(orderId)
                .orderNo(generateOrderNo(orderId))
                .memberId(member.getId())
                .memberName(member.getUsername())
                .phone(member.getPhone())
                .wechatId(member.getWechatId())
                .wechatIdIndex(member.getWechatIdIndex())
                .orderType(orderType)
                .beforeType(currentType)
                .afterType(targetType)
                .durationMonths(dto.getDurationMonths())
                .amount(finalAmount)
                .beforeExpireAt(beforeExpireAt)
                .afterExpireAt(newExpireAt)
                .operatorId(SecurityUtil.getCurrentAdminId())
                .operatorName(operatorName)
                .remark(dto.getRemark())
                .status(OrderStatus.PENDING)
                .paymentMethod(StringUtils.hasText(dto.getPaymentMethod()) ? dto.getPaymentMethod() : "offline")
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        memberOrderMapper.insert(order);

        log.info("创建待支付订单成功: orderId={}, memberId={}, targetType={}, amount={}",
                orderId, dto.getMemberId(), dto.getTargetType(), finalAmount);

        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long id) {
        // 1. 查订单，校验 status == PENDING
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(400, "订单状态不是待支付，无法确认");
        }

        // 2. 查用户，校验存在
        Member member = memberMapper.selectById(order.getMemberId());
        if (member == null || member.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 3. 更新用户表: memberType = order.afterType, expireAt = order.afterExpireAt
        OffsetDateTime now = OffsetDateTime.now();

        // Pro → VIP 升级：暂存Pro剩余时间，VIP从现在开始
        MemberType beforeType = order.getBeforeType();
        MemberType afterType = order.getAfterType();
        boolean isProToVip = (beforeType == MemberType.PRO && afterType == MemberType.VIP);
        boolean isProActive = beforeType == MemberType.PRO
                && (order.getBeforeExpireAt() == null || order.getBeforeExpireAt().isAfter(now));

        if (isProToVip && isProActive) {
            // 计算Pro剩余时间（永久Pro: 给一个合理的默认值，如12个月）
            OffsetDateTime beforeExpireAt = order.getBeforeExpireAt();
            int remainingMonths;
            if (beforeExpireAt == null) {
                // 永久Pro：默认挂起12个月
                remainingMonths = 12;
            } else {
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(now, beforeExpireAt);
                remainingMonths = (int) Math.max(1, Math.round(daysBetween / 30.0));
            }
            member.setSuspendedMemberType(MemberType.PRO.getValue());
            member.setSuspendedExpireAt(beforeExpireAt);
            member.setSuspendedRemainingMonths(remainingMonths);
            log.info("Pro→VIP升级(订单确认)，暂存Pro: memberId={}, beforeExpireAt={}, remainingMonths={}",
                    member.getId(), beforeExpireAt, remainingMonths);
        }

        member.setMemberType(order.getAfterType().getValue());
        member.setExpireAt(order.getAfterExpireAt());
        member.setUpdatedAt(now);
        int affected = memberMapper.updateById(member);
        if (affected == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }

        // 会员类型变更属权限授予，不 bump tokenVersion（避免生产 Redis 版本号使旧 token 失效）。VIP 实时性由 AuthAspect.checkVip 回退 DB 查询保证。

        // 4. 更新订单 status = COMPLETED
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(now);
        int orderAffected = memberOrderMapper.updateById(order);
        if (orderAffected == 0) {
            throw new BusinessException(400, "订单数据已被其他人修改，请刷新后重试");
        }

        // 5. 处理佣金（如有推荐人）
        if (member.getReferrerId() != null) {
            SystemSettings settings = settingsMapper.selectOne(
                    new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));
            if (settings != null) {
                processCommission(member, order, settings);
            }
        }

        // 6. 发送通知
        NotificationType notificationType = order.getOrderType() == OrderType.NEW
                ? NotificationType.MEMBER_ACTIVATION_SUCCESS
                : NotificationType.MEMBER_RENEWED;
        String title = order.getOrderType() == OrderType.NEW ? "会员开通成功" : "会员续费成功";
        String content = String.format("您的%s会员已%s，有效期至%s",
                order.getAfterType().getDesc(),
                order.getOrderType() == OrderType.NEW ? "开通" : "续费",
                order.getAfterExpireAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        notificationService.sendNotification(member.getId(), notificationType, title, content, order.getId());

        log.info("确认订单支付成功: orderId={}, memberId={}", id, member.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        // 1. 查订单，校验 status == PENDING
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(400, "订单状态不是待支付，无法取消");
        }

        // 2. 更新订单 status = CANCELLED（不影响用户，PENDING订单未变更用户状态）
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(OffsetDateTime.now());
        int orderAffected = memberOrderMapper.updateById(order);
        if (orderAffected == 0) {
            throw new BusinessException(400, "订单数据已被其他人修改，请刷新后重试");
        }

        log.info("取消订单成功: orderId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeOrder(Long id, String remark) {
        // 1. 查订单，校验 status == COMPLETED
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(400, "订单状态不是已完成，无法撤销");
        }

        // 2. 查用户，校验存在
        Member member = memberMapper.selectById(order.getMemberId());
        if (member == null || member.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 3. 回退用户: memberType = order.beforeType, expireAt = order.beforeExpireAt
        OffsetDateTime now = OffsetDateTime.now();
        member.setMemberType(order.getBeforeType().getValue());
        member.setExpireAt(order.getBeforeExpireAt());
        // 撤销Pro→VIP订单时，清除挂起字段
        if (order.getBeforeType() == MemberType.PRO && order.getAfterType() == MemberType.VIP) {
            member.setSuspendedMemberType(null);
            member.setSuspendedExpireAt(null);
            member.setSuspendedRemainingMonths(null);
        }
        member.setUpdatedAt(now);
        int affected = memberMapper.updateById(member);
        if (affected == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }

        // 撤销会员类型变更同样不 bump tokenVersion（VIP 实时性由 AuthAspect.checkVip 回退 DB 查询保证）。

        // 4. 更新订单 status = REVOKED，追加 remark
        String finalRemark = order.getRemark();
        if (StringUtils.hasText(remark)) {
            finalRemark = StringUtils.hasText(finalRemark)
                    ? finalRemark + " | 撤销原因: " + remark
                    : "撤销原因: " + remark;
        }
        order.setStatus(OrderStatus.REVOKED);
        order.setRemark(finalRemark);
        order.setUpdatedAt(now);
        int orderAffected = memberOrderMapper.updateById(order);
        if (orderAffected == 0) {
            throw new BusinessException(400, "订单数据已被其他人修改，请刷新后重试");
        }

        // 5. 回退佣金（如有）：扣减推荐人余额，软删除佣金记录
        revertCommission(order);

        // 6. 发送通知给用户
        String title = "会员已撤销";
        String content = String.format("您的%s会员已被撤销，会员类型已回退为%s",
                order.getAfterType().getDesc(),
                order.getBeforeType().getDesc());
        notificationService.sendNotification(member.getId(), NotificationType.MEMBER_REVOKED, title, content, order.getId());

        log.info("撤销订单成功: orderId={}, memberId={}", id, member.getId());
    }

    // ==================== 删除方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        MemberOrder order = memberOrderMapper.selectByIdIgnoreDeleted(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }

        Long commissionCount = commissionMapper.selectCount(
                new LambdaQueryWrapper<ReferralCommission>()
                        .eq(ReferralCommission::getOrderId, id)
                        .eq(ReferralCommission::getDeleted, false));
        if (commissionCount > 0) {
            throw new BusinessException(400, "该订单存在关联的佣金记录，无法硬删除");
        }

        memberOrderMapper.hardDeleteById(id);
        log.info("硬删除订单成功: orderId={}", id);
    }

    // ==================== 私有工具方法 ====================

    /**
     * 根据会员类型和时长自动计算金额
     */
    private BigDecimal calculateAmount(SystemSettings settings, String targetType, Integer durationMonths) {
        Integer yearPrice;
        if ("vip".equals(targetType)) {
            yearPrice = settings.getVipPrice();
        } else {
            yearPrice = settings.getProPrice();
        }

        if (yearPrice == null || yearPrice <= 0) {
            throw new BusinessException(500, "会员价格未设置");
        }

        return new BigDecimal(yearPrice)
                .divide(new BigDecimal(12), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(durationMonths))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 生成订单号: ORD + yyyyMMdd + 雪花ID后8位
     */
    private String generateOrderNo(Long orderId) {
        String dateStr = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String idSuffix = String.valueOf(orderId);
        if (idSuffix.length() > 8) {
            idSuffix = idSuffix.substring(idSuffix.length() - 8);
        }
        return "ORD" + dateStr + idSuffix;
    }

    /**
     * 处理推荐佣金
     */
    private void processCommission(Member referee, MemberOrder order, SystemSettings settings) {
        Member referrer = memberMapper.selectById(referee.getReferrerId());
        if (referrer == null || referrer.getDeleted()) {
            log.warn("推荐人不存在或已删除: referrerId={}", referee.getReferrerId());
            return;
        }

        if (!referrer.isActive()) {
            log.warn("推荐人已被禁用，不发放佣金: referrerId={}", referee.getReferrerId());
            return;
        }

        String targetType = order.getAfterType().getValue();
        Integer commissionRatePercent;
        if ("vip".equals(targetType)) {
            commissionRatePercent = settings.getVipCommissionRate();
        } else {
            commissionRatePercent = settings.getProCommissionRate();
        }

        if (commissionRatePercent == null || commissionRatePercent <= 0) {
            log.info("佣金比例为0或未设置，跳过佣金处理: targetType={}", targetType);
            return;
        }

        BigDecimal commissionRate = new BigDecimal(commissionRatePercent).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
        BigDecimal commissionAmount = order.getAmount().multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);

        if (commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("佣金金额为0，跳过佣金处理");
            return;
        }

        BigDecimal newBalance = referrer.getCommissionBalance() != null
                ? referrer.getCommissionBalance().add(commissionAmount)
                : commissionAmount;
        BigDecimal newTotalEarned = referrer.getCommissionTotalEarned() != null
                ? referrer.getCommissionTotalEarned().add(commissionAmount)
                : commissionAmount;

        referrer.setCommissionBalance(newBalance);
        referrer.setCommissionTotalEarned(newTotalEarned);
        referrer.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(referrer);

        ReferralCommission commission = ReferralCommission.builder()
                .id(SnowflakeIdGenerator.nextId())
                .referrerId(referrer.getId())
                .referrerName(referrer.getUsername())
                .referrerPhone(referrer.getPhone())
                .refereeId(referee.getId())
                .refereeName(referee.getUsername())
                .refereePhone(referee.getPhone())
                .orderId(order.getId())
                .orderAmount(order.getAmount())
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmount)
                .deleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        commissionMapper.insert(commission);

        String title = "佣金到账";
        String content = String.format("您推荐的用户%s已开通%s会员，佣金%.2f元已到账",
                DesensitizeUtil.desensitizeName(referee.getUsername()),
                MemberType.fromValue(targetType).getDesc(),
                commissionAmount);
        notificationService.sendNotification(referrer.getId(), NotificationType.COMMISSION_EARNED, title, content, commission.getId());

        log.info("佣金处理成功: referrerId={}, refereeId={}, commissionAmount={}",
                referrer.getId(), referee.getId(), commissionAmount);
    }

    /**
     * 回退佣金：扣减推荐人余额，软删除佣金记录
     */
    private void revertCommission(MemberOrder order) {
        ReferralCommission commission = commissionMapper.selectOne(
                new LambdaQueryWrapper<ReferralCommission>()
                        .eq(ReferralCommission::getOrderId, order.getId())
                        .eq(ReferralCommission::getDeleted, false)
                        .eq(ReferralCommission::getStatus, "active"));
        if (commission == null) {
            return;
        }

        Member referrer = memberMapper.selectById(commission.getReferrerId());
        if (referrer != null && !referrer.getDeleted()) {
            BigDecimal newBalance = referrer.getCommissionBalance() != null
                    ? referrer.getCommissionBalance().subtract(commission.getCommissionAmount())
                    : BigDecimal.ZERO.subtract(commission.getCommissionAmount());
            BigDecimal newTotalEarned = referrer.getCommissionTotalEarned() != null
                    ? referrer.getCommissionTotalEarned().subtract(commission.getCommissionAmount())
                    : BigDecimal.ZERO.subtract(commission.getCommissionAmount());
            BigDecimal currentPaid = referrer.getCommissionTotalPaid() != null
                    ? referrer.getCommissionTotalPaid() : BigDecimal.ZERO;
            BigDecimal paidReduction = commission.getCommissionAmount().min(currentPaid);

            referrer.setCommissionBalance(newBalance);
            referrer.setCommissionTotalEarned(newTotalEarned);
            referrer.setCommissionTotalPaid(currentPaid.subtract(paidReduction));
            referrer.setUpdatedAt(OffsetDateTime.now());
            memberMapper.updateById(referrer);

            // 通知推荐人佣金被撤回
            String title = "佣金已撤回";
            String content = newBalance.compareTo(BigDecimal.ZERO) < 0
                    ? String.format("因订单撤销，您的佣金%.2f元已被扣回，当前佣金余额为%.2f元（欠款将在后续佣金中自动抵扣）",
                            commission.getCommissionAmount(), newBalance)
                    : String.format("因订单撤销，您的佣金%.2f元已被扣回，当前佣金余额为%.2f元",
                            commission.getCommissionAmount(), newBalance);
            notificationService.sendNotification(referrer.getId(),
                    NotificationType.COMMISSION_REVERSED, title, content, order.getId());
        }

        // 标记佣金为已撤回（保留记录，不软删除）
        commission.setStatus("revoked");
        commission.setUpdatedAt(OffsetDateTime.now());
        commissionMapper.updateById(commission);

        log.info("回退佣金成功: orderId={}, commissionId={}, amount={}",
                order.getId(), commission.getId(), commission.getCommissionAmount());
    }
}
