package com.haifeng.admin.service.impl.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.common.mapper.dashboard.DashboardMapper;
import com.haifeng.admin.service.dashboard.DashboardService;
import com.haifeng.admin.vo.dashboard.DashboardOverviewVO;
import com.haifeng.admin.vo.dashboard.DashboardStatsVO;
import com.haifeng.admin.vo.dashboard.SystemInfoVO;
import com.haifeng.admin.vo.dashboard.TodoListVO;
import com.haifeng.admin.vo.dashboard.TrendDataVO;
import com.haifeng.common.entity.permission.SysAdmin;
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
import com.haifeng.common.enums.WithdrawStatus;
import com.haifeng.common.entity.user.WithdrawRecord;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.user.WithdrawRecordMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberMapper memberMapper;
    private final MemberOrderMapper memberOrderMapper;
    private final WithdrawRecordMapper withdrawRecordMapper;
    private final UniversityMapper universityMapper;
    private final MajorMapper majorMapper;
    private final IndustryMapper industryMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final AdmissionGroupMapper admissionGroupMapper;
    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final SystemSettingsMapper systemSettingsMapper;
    private final SysAdminMapper sysAdminMapper;
    private final DashboardMapper dashboardMapper;
    private final ObjectProvider<BuildProperties> buildPropertiesProvider;

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setMemberStats(getMemberStats());
        vo.setOrderStats(getOrderStats());
        vo.setEntityStats(getEntityStats());
        return vo;
    }

    @Override
    public TrendDataVO getMemberTrend(int days) {
        days = clampDays(days);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Map<String, Object>> rawList = dashboardMapper.countMembersByDate(
            startDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(),
            endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime()
        );

        return buildTrendData(startDate, days, rawList);
    }

    @Override
    public TrendDataVO getOrderTrend(int days) {
        days = clampDays(days);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Map<String, Object>> rawList = dashboardMapper.countOrdersByDate(
            startDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(),
            endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime()
        );

        return buildTrendData(startDate, days, rawList);
    }

    @Override
    public DashboardOverviewVO getDashboardOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setSystemInfo(getSystemInfo());
        vo.setTodoList(getTodoList());
        return vo;
    }

    private SystemInfoVO getSystemInfo() {
        SystemInfoVO info = new SystemInfoVO();
        BuildProperties bp = buildPropertiesProvider.getIfAvailable();
        info.setAppVersion(bp != null ? bp.getVersion() : "1.0.0");
        info.setSpringVersion("3.3.5");
        info.setJavaVersion("17");

        SystemSettings settings = systemSettingsMapper.selectById(1L);
        if (settings != null) {
            info.setSiteName(settings.getSiteName());
            info.setAiProvider(settings.getProviderName());
            info.setAiModel(settings.getModelName());
        }

        info.setAdminCount(sysAdminMapper.selectCount(
            new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getStatus, 1)));

        return info;
    }

    private TodoListVO getTodoList() {
        TodoListVO todo = new TodoListVO();

        // 待处理订单数
        todo.setPendingOrderCount(memberOrderMapper.selectCount(
            new LambdaQueryWrapper<MemberOrder>()
                .eq(MemberOrder::getStatus, OrderStatus.PENDING)
                .eq(MemberOrder::getDeleted, false)));

        // 最新 3 条待处理订单
        List<MemberOrder> recentOrders = memberOrderMapper.selectList(
            new LambdaQueryWrapper<MemberOrder>()
                .eq(MemberOrder::getStatus, OrderStatus.PENDING)
                .eq(MemberOrder::getDeleted, false)
                .orderByDesc(MemberOrder::getCreatedAt)
                .last("LIMIT 3"));

        todo.setPendingOrders(recentOrders.stream().map(order -> {
            TodoListVO.PendingOrderItem item = new TodoListVO.PendingOrderItem();
            item.setId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setMemberName(order.getMemberName());
            item.setAmount(order.getAmount());
            item.setCreatedAt(order.getCreatedAt().toString());
            return item;
        }).collect(Collectors.toList()));

        // 待处理提现数
        todo.setPendingWithdrawCount(withdrawRecordMapper.selectCount(
            new LambdaQueryWrapper<WithdrawRecord>()
                .eq(WithdrawRecord::getStatus, WithdrawStatus.PENDING)
                .eq(WithdrawRecord::getDeleted, false)));

        // 最新 3 条待处理提现
        List<WithdrawRecord> recentWithdraws = withdrawRecordMapper.selectList(
            new LambdaQueryWrapper<WithdrawRecord>()
                .eq(WithdrawRecord::getStatus, WithdrawStatus.PENDING)
                .eq(WithdrawRecord::getDeleted, false)
                .orderByDesc(WithdrawRecord::getCreatedAt)
                .last("LIMIT 3"));

        todo.setPendingWithdraws(recentWithdraws.stream().map(withdraw -> {
            TodoListVO.PendingWithdrawItem item = new TodoListVO.PendingWithdrawItem();
            item.setId(withdraw.getId());
            item.setMemberName(withdraw.getMemberName());
            item.setAmount(withdraw.getAmount());
            item.setCreatedAt(withdraw.getCreatedAt().toString());
            return item;
        }).collect(Collectors.toList()));

        return todo;
    }

    private int clampDays(int days) {
        if (days <= 7) return 7;
        if (days <= 30) return 30;
        return 90;
    }

    private TrendDataVO buildTrendData(LocalDate startDate, int days, List<Map<String, Object>> rawList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : rawList) {
            String dateStr = row.get("date").toString();
            Long count = ((Number) row.get("count")).longValue();
            countMap.put(dateStr, count);
        }

        List<String> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.format(formatter);
            dates.add(dateStr);
            values.add(countMap.getOrDefault(dateStr, 0L));
        }

        TrendDataVO vo = new TrendDataVO();
        vo.setDates(dates);
        vo.setValues(values);
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
