package com.haifeng.admin.service.impl.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.service.dashboard.DashboardService;
import com.haifeng.admin.vo.dashboard.DashboardStatsVO;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.industry.Industry;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.enums.OrderStatus;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberMapper memberMapper;
    private final MemberOrderMapper memberOrderMapper;
    private final UniversityMapper universityMapper;
    private final MajorMapper majorMapper;
    private final IndustryMapper industryMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final AdmissionGroupMapper admissionGroupMapper;
    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final SystemSettingsMapper systemSettingsMapper;

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setMemberStats(getMemberStats());
        vo.setOrderStats(getOrderStats());
        vo.setEntityStats(getEntityStats());
        return vo;
    }

    private DashboardStatsVO.MemberStats getMemberStats() {
        DashboardStatsVO.MemberStats stats = new DashboardStatsVO.MemberStats();

        stats.setTotalMembers(memberMapper.selectCount(
            new LambdaQueryWrapper<Member>().eq(Member::getDeleted, false)));

        stats.setProMembers(memberMapper.selectCount(
            new LambdaQueryWrapper<Member>()
                .eq(Member::getDeleted, false)
                .eq(Member::getMemberType, "pro")));

        stats.setVipMembers(memberMapper.selectCount(
            new LambdaQueryWrapper<Member>()
                .eq(Member::getDeleted, false)
                .eq(Member::getMemberType, "vip")
                .gt(Member::getExpireAt, OffsetDateTime.now())));

        return stats;
    }

    private DashboardStatsVO.OrderStats getOrderStats() {
        DashboardStatsVO.OrderStats stats = new DashboardStatsVO.OrderStats();

        stats.setPendingOrders(memberOrderMapper.selectCount(
            new LambdaQueryWrapper<MemberOrder>()
                .eq(MemberOrder::getDeleted, false)
                .eq(MemberOrder::getStatus, OrderStatus.PENDING)));

        SystemSettings settings = systemSettingsMapper.selectById(1L);
        stats.setTotalAmount(settings != null ? settings.getTotalAmount() : BigDecimal.ZERO);

        return stats;
    }

    private DashboardStatsVO.EntityStats getEntityStats() {
        DashboardStatsVO.EntityStats stats = new DashboardStatsVO.EntityStats();

        stats.setUniversityCount(universityMapper.selectCount(null));
        stats.setMajorCount(majorMapper.selectCount(null));
        stats.setIndustryCount(industryMapper.selectCount(
            new LambdaQueryWrapper<Industry>().eq(Industry::getIsDeleted, false)));
        stats.setEnterpriseCount(enterpriseMapper.selectCount(
            new LambdaQueryWrapper<Enterprise>().eq(Enterprise::getIsDeleted, false)));
        stats.setAdmissionGroupCount(admissionGroupMapper.selectCount(
            new LambdaQueryWrapper<AdmissionGroup>().eq(AdmissionGroup::getIsDeleted, false)));
        stats.setAdmissionMajorScoreCount(admissionMajorScoreMapper.selectCount(
            new LambdaQueryWrapper<AdmissionMajorScore>().eq(AdmissionMajorScore::getIsDeleted, false)));

        return stats;
    }
}
