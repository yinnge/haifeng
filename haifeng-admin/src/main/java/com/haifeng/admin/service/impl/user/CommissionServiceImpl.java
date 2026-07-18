package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.CommissionQueryDTO;
import com.haifeng.admin.service.user.CommissionService;
import com.haifeng.admin.vo.user.CommissionListVO;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.entity.user.ReferralCommission;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.user.ReferralCommissionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final ReferralCommissionMapper referralCommissionMapper;
    private final MemberOrderMapper memberOrderMapper;

    @Override
    public IPage<CommissionListVO> page(CommissionQueryDTO dto) {
        Page<ReferralCommission> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<ReferralCommission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReferralCommission::getDeleted, false);

        if (StringUtils.hasText(dto.getReferrerPhone())) {
            wrapper.likeLeft(ReferralCommission::getReferrerPhone, dto.getReferrerPhone());
        }
        if (StringUtils.hasText(dto.getReferrerName())) {
            wrapper.like(ReferralCommission::getReferrerName, dto.getReferrerName());
        }
        if (StringUtils.hasText(dto.getRefereePhone())) {
            wrapper.likeLeft(ReferralCommission::getRefereePhone, dto.getRefereePhone());
        }
        if (StringUtils.hasText(dto.getRefereeName())) {
            wrapper.like(ReferralCommission::getRefereeName, dto.getRefereeName());
        }
        if (StringUtils.hasText(dto.getOrderNo())) {
            List<Long> orderIds = findOrderIdsByOrderNo(dto.getOrderNo());
            if (orderIds.isEmpty()) {
                return new Page<CommissionListVO>().setRecords(List.of());
            }
            wrapper.in(ReferralCommission::getOrderId, orderIds);
        }

        wrapper.orderByDesc(ReferralCommission::getCreatedAt);

        IPage<ReferralCommission> commissionPage = referralCommissionMapper.selectPage(page, wrapper);

        return commissionPage.convert(commission -> {
            CommissionListVO vo = new CommissionListVO();
            vo.setId(commission.getId());
            vo.setReferrerName(commission.getReferrerName());
            vo.setReferrerPhone(commission.getReferrerPhone());
            vo.setRefereeName(commission.getRefereeName());
            vo.setRefereePhone(commission.getRefereePhone());
            vo.setOrderId(commission.getOrderId());
            vo.setOrderAmount(commission.getOrderAmount());
            vo.setCommissionRate(commission.getCommissionRate());
            vo.setCommissionAmount(commission.getCommissionAmount());
            vo.setCreatedAt(commission.getCreatedAt());
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ReferralCommission commission = referralCommissionMapper.selectById(id);
        if (commission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "佣金记录不存在");
        }

        commission.setDeleted(true);
        commission.setUpdatedAt(OffsetDateTime.now());
        referralCommissionMapper.updateById(commission);

        log.info("删除佣金记录成功: commissionId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        ReferralCommission commission = referralCommissionMapper.selectByIdIgnoreDeleted(id);
        if (commission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "佣金记录不存在");
        }

        if (!commission.getDeleted()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先禁用该佣金记录，再执行硬删除");
        }

        referralCommissionMapper.hardDeleteById(id);
        log.info("硬删除佣金记录成功: commissionId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        ReferralCommission commission = referralCommissionMapper.selectByIdIgnoreDeleted(id);
        if (commission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "佣金记录不存在");
        }

        if (!commission.getDeleted()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该佣金记录未被禁用，无需恢复");
        }

        referralCommissionMapper.restoreById(id, OffsetDateTime.now());
        log.info("恢复佣金记录成功: commissionId={}", id);
    }

    private List<Long> findOrderIdsByOrderNo(String orderNo) {
        LambdaQueryWrapper<MemberOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberOrder::getDeleted, false);
        wrapper.like(MemberOrder::getOrderNo, orderNo);
        wrapper.select(MemberOrder::getId);
        List<MemberOrder> orders = memberOrderMapper.selectList(wrapper);
        return orders.stream().map(MemberOrder::getId).collect(Collectors.toList());
    }
}
