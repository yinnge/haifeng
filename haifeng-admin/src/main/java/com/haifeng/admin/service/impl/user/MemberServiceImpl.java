package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.MemberQueryDTO;
import com.haifeng.admin.dto.user.MemberStatusDTO;
import com.haifeng.admin.dto.user.MemberUpgradeDTO;
import com.haifeng.admin.service.user.MemberService;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.vo.user.MemberDetailVO;
import com.haifeng.admin.vo.user.MemberListVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.entity.user.ReferralCommission;
import com.haifeng.common.enums.MemberType;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.enums.OrderType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.user.ReferralCommissionMapper;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final MemberOrderMapper orderMapper;
    private final ReferralCommissionMapper commissionMapper;
    private final SystemSettingsMapper settingsMapper;
    private final NotificationService notificationService;
    private final SecurityProperties securityProperties;

    @Override
    public IPage<MemberListVO> page(MemberQueryDTO dto) {
        Page<Member> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, false);

        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(Member::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getMemberType())) {
            wrapper.eq(Member::getMemberType, dto.getMemberType());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Member::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getWechatId())) {
            // 将微信号转换为盲索引进行等值查询
            String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
            wrapper.eq(Member::getWechatIdIndex, blindIndex);
        }
        if (StringUtils.hasText(dto.getInviteCode())) {
            wrapper.like(Member::getInviteCode, dto.getInviteCode());
        }

        wrapper.orderByDesc(Member::getCreatedAt);

        IPage<Member> memberPage = memberMapper.selectPage(page, wrapper);

        return memberPage.convert(member -> {
            MemberListVO vo = new MemberListVO();
            BeanUtils.copyProperties(member, vo);
            // 微信号脱敏
            vo.setWechatId(DesensitizeUtil.desensitizeWechat(member.getWechatId()));
            return vo;
        });
    }

    @Override
    public MemberDetailVO detail(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        MemberDetailVO vo = new MemberDetailVO();
        BeanUtils.copyProperties(member, vo);
        // 微信号脱敏
        vo.setWechatId(DesensitizeUtil.desensitizeWechat(member.getWechatId()));
        return vo;
    }

    @Override
    public void updateStatus(Long id, MemberStatusDTO dto) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        member.setStatus(dto.getStatus());
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("修改用户状态成功: userId={}, status={}", id, dto.getStatus());
    }

    @Override
    public String getWechatPlaintext(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 微信号已通过 TypeHandler 自动解密
        return member.getWechatId();
    }

    @Override
    @Transactional
    public Long upgradeMember(Long id, MemberUpgradeDTO dto) {
        // 1. 校验用户存在
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 2. 校验目标类型合法性（vip不能降级为pro）
        MemberType currentType = MemberType.fromValue(member.getMemberType());
        MemberType targetType = MemberType.fromValue(dto.getTargetType());

        if (currentType == MemberType.VIP && targetType == MemberType.PRO) {
            // VIP用户只有在会员过期后才允许降级
            if (member.getExpireAt() != null && member.getExpireAt().isAfter(OffsetDateTime.now())) {
                throw new BusinessException(400, "VIP会员未过期，不能降级为Pro");
            }
        }

        // 3. 计算到期时间
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime beforeExpireAt = member.getExpireAt();
        OffsetDateTime newExpireAt;

        // 判断是否过期或新开通
        boolean isExpired = beforeExpireAt == null || beforeExpireAt.isBefore(now);
        boolean isTypeChange = currentType != targetType;

        if (isExpired || isTypeChange) {
            // 已过期或类型变更：从当前时间开始计算
            newExpireAt = now.plusMonths(dto.getDurationMonths());
        } else {
            // 未过期且同类型续费：从原到期时间叠加
            newExpireAt = beforeExpireAt.plusMonths(dto.getDurationMonths());
        }

        // 4. 判断订单类型：同类型=RENEWAL，不同类型=NEW
        OrderType orderType;
        if (currentType == MemberType.NORMAL || isTypeChange) {
            orderType = OrderType.NEW;
        } else {
            orderType = OrderType.RENEWAL;
        }

        // 5. 计算金额（如果未传入，则自动计算）
        BigDecimal finalAmount = dto.getAmount();
        if (finalAmount == null) {
            finalAmount = calculateAmount(dto.getTargetType(), dto.getDurationMonths());
        }

        // 6. 更新用户表
        member.setMemberType(dto.getTargetType());
        member.setExpireAt(newExpireAt);
        member.setUpdatedAt(now);
        memberMapper.updateById(member);

        // 7. 创建订单记录
        Long orderId = SnowflakeIdGenerator.nextId();
        Long operatorId = SecurityUtil.getCurrentAdminId();
        String operatorName = SecurityUtil.getCurrentUser() != null ? SecurityUtil.getCurrentUser().getUsername() : null;

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
                .operatorId(operatorId)
                .operatorName(operatorName)
                .remark(dto.getRemark())
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        orderMapper.insert(order);

        // 8. 处理佣金（如有推荐人）
        if (member.getReferrerId() != null) {
            processCommission(member, order, dto.getTargetType());
        }

        // 9. 发送通知
        NotificationType notificationType = orderType == OrderType.NEW
                ? NotificationType.MEMBER_ACTIVATION_SUCCESS
                : NotificationType.MEMBER_RENEWED;
        String title = orderType == OrderType.NEW ? "会员开通成功" : "会员续费成功";
        String content = String.format("您的%s会员已%s，有效期至%s",
                targetType.getDesc(),
                orderType == OrderType.NEW ? "开通" : "续费",
                newExpireAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        notificationService.sendNotification(member.getId(), notificationType, title, content, orderId);

        log.info("会员开通/续费成功: userId={}, orderType={}, targetType={}, orderId={}, amount={}",
                id, orderType.getValue(), dto.getTargetType(), orderId, finalAmount);

        // 10. 返回订单ID
        return orderId;
    }

    /**
     * 根据会员类型和时长自动计算金额
     * 公式：(年价格 / 12) * 月数
     */
    private BigDecimal calculateAmount(String targetType, Integer durationMonths) {
        SystemSettings settings = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));
        if (settings == null) {
            throw new BusinessException(500, "系统设置不存在，无法计算金额");
        }

        Integer yearPrice;
        if ("vip".equals(targetType)) {
            yearPrice = settings.getVipPrice();
        } else {
            yearPrice = settings.getProPrice();
        }

        if (yearPrice == null || yearPrice <= 0) {
            throw new BusinessException(500, "会员价格未设置");
        }

        // (年价格 / 12) * 月数，保留2位小数
        return new BigDecimal(yearPrice)
                .divide(new BigDecimal(12), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(durationMonths))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 生成订单号
     * 格式：ORD + yyyyMMdd + 雪花ID后8位
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
    private void processCommission(Member referee, MemberOrder order, String targetType) {
        // 1. 获取推荐人
        Member referrer = memberMapper.selectById(referee.getReferrerId());
        if (referrer == null || referrer.getDeleted()) {
            log.warn("推荐人不存在或已删除: referrerId={}", referee.getReferrerId());
            return;
        }

        // 2. 检查推荐人是否被禁用
        if (!referrer.isActive()) {
            log.warn("推荐人已被禁用，不发放佣金: referrerId={}", referee.getReferrerId());
            return;
        }

        // 2. 获取佣金比例（pro用proCommissionRate，vip用vipCommissionRate）
        SystemSettings settings = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));
        if (settings == null) {
            log.warn("系统设置不存在，无法计算佣金");
            return;
        }

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

        // 3. 计算佣金金额
        BigDecimal commissionRate = new BigDecimal(commissionRatePercent).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
        BigDecimal commissionAmount = order.getAmount().multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);

        if (commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("佣金金额为0，跳过佣金处理");
            return;
        }

        // 4. 更新推荐人余额（commissionBalance, commissionTotalEarned）
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

        // 5. 创建佣金记录
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

        // 6. 发送通知给推荐人
        String title = "佣金到账";
        String content = String.format("您推荐的用户%s已开通%s会员，佣金%.2f元已到账",
                DesensitizeUtil.desensitizeName(referee.getUsername()),
                MemberType.fromValue(targetType).getDesc(),
                commissionAmount);
        notificationService.sendNotification(referrer.getId(), NotificationType.COMMISSION_EARNED, title, content, commission.getId());

        log.info("佣金处理成功: referrerId={}, refereeId={}, commissionAmount={}",
                referrer.getId(), referee.getId(), commissionAmount);
    }
}
