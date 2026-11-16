package com.haifeng.app.service.consultation;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.consultation.ConsultationAddDTO;
import com.haifeng.app.dto.consultation.ConsultationQueryDTO;
import com.haifeng.app.service.impl.consultation.ConsultationServiceImpl;
import com.haifeng.app.vo.consultation.ConsultationDetailVO;
import com.haifeng.app.vo.consultation.ConsultationListVO;
import com.haifeng.common.entity.consultation.Consultation;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberNotification;
import com.haifeng.common.enums.ConsultationBizType;
import com.haifeng.common.enums.ConsultationStatus;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.consultation.ConsultationMapper;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberNotificationMapper;
import com.haifeng.common.security.AuthUser;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceImplTest {

    @Mock private ConsultationMapper consultationMapper;
    @Mock private PlannerMapper plannerMapper;
    @Mock private InstitutionMapper institutionMapper;
    @Mock private MemberMapper memberMapper;
    @Mock private MemberNotificationMapper memberNotificationMapper;

    @InjectMocks private ConsultationServiceImpl service;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        // 单测没有 Spring 容器，LambdaQueryWrapper 需要手动初始化实体元数据
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), Consultation.class);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(Long memberId) {
        AuthUser user = AuthUser.builder()
                .userId(memberId)
                .userType("member")
                .memberType("normal")
                .teacher(true)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private static ConsultationAddDTO dto() {
        ConsultationAddDTO dto = new ConsultationAddDTO();
        dto.setMessage("  想咨询高考志愿  ");
        dto.setWechat("  wx_abc  ");
        dto.setPhone("13800000000");
        return dto;
    }

    private static Member teacher(Long id) {
        return Member.builder().id(id).teacher(true).deleted(false).username("teacher" + id).build();
    }

    private static Consultation consultation(Long id, Long teacherMemberId, ConsultationStatus status) {
        return Consultation.builder()
                .id(id)
                .bizType(ConsultationBizType.PLANNER)
                .bizId(10L)
                .bizName("张老师")
                .teacherMemberId(teacherMemberId)
                .senderMemberId(null)
                .message("想咨询高考志愿")
                .wechat("wx_abc")
                .phone("13800000000")
                .clientIp("127.0.0.1")
                .status(status)
                .version(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void submit_planner_success_insertsConsultationAndNotification() {
        when(plannerMapper.selectById(10L)).thenReturn(Planner.builder()
                .id(10L).userId(100L).name("张老师").deleted(false).build());
        when(memberMapper.selectById(100L)).thenReturn(teacher(100L));

        Long id = service.submit(ConsultationBizType.PLANNER, 10L, dto());

        assertThat(id).isNotNull();

        ArgumentCaptor<Consultation> consultationCaptor = ArgumentCaptor.forClass(Consultation.class);
        verify(consultationMapper).insert(consultationCaptor.capture());
        Consultation saved = consultationCaptor.getValue();
        assertThat(saved.getBizType()).isEqualTo(ConsultationBizType.PLANNER);
        assertThat(saved.getBizId()).isEqualTo(10L);
        assertThat(saved.getBizName()).isEqualTo("张老师");
        assertThat(saved.getTeacherMemberId()).isEqualTo(100L);
        assertThat(saved.getSenderMemberId()).isNull();
        assertThat(saved.getMessage()).isEqualTo("想咨询高考志愿");
        assertThat(saved.getWechat()).isEqualTo("wx_abc");
        assertThat(saved.getPhone()).isEqualTo("13800000000");
        assertThat(saved.getStatus()).isEqualTo(ConsultationStatus.PENDING);
        assertThat(saved.getVersion()).isZero();
        assertThat(saved.getClientIp()).isEqualTo("unknown");

        ArgumentCaptor<MemberNotification> notificationCaptor =
                ArgumentCaptor.forClass(MemberNotification.class);
        verify(memberNotificationMapper).insert(notificationCaptor.capture());
        MemberNotification notification = notificationCaptor.getValue();
        assertThat(notification.getMemberId()).isEqualTo(100L);
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.CONSULTATION_RECEIVED);
        assertThat(notification.getRelatedId()).isEqualTo(id);
        assertThat(notification.getIsRead()).isFalse();
        assertThat(notification.getContent())
                .contains("张老师", "想咨询高考志愿", "微信号：wx_abc", "电话：13800000000");
    }

    @Test
    void submit_loggedIn_recordsSenderMemberId() {
        loginAs(500L);
        when(plannerMapper.selectById(10L)).thenReturn(Planner.builder()
                .id(10L).userId(100L).name("张老师").deleted(false).build());
        when(memberMapper.selectById(100L)).thenReturn(teacher(100L));

        service.submit(ConsultationBizType.PLANNER, 10L, dto());

        ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
        verify(consultationMapper).insert(captor.capture());
        assertThat(captor.getValue().getSenderMemberId()).isEqualTo(500L);
    }

    @Test
    void submit_institution_success_usesInstitutionOwner() {
        when(institutionMapper.selectById(20L)).thenReturn(Institution.builder()
                .id(20L).userId(200L).name("冲刺小班").status((short) 1).build());
        when(memberMapper.selectById(200L)).thenReturn(teacher(200L));

        service.submit(ConsultationBizType.INSTITUTION, 20L, dto());

        ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
        verify(consultationMapper).insert(captor.capture());
        assertThat(captor.getValue().getBizType()).isEqualTo(ConsultationBizType.INSTITUTION);
        assertThat(captor.getValue().getTeacherMemberId()).isEqualTo(200L);
        assertThat(captor.getValue().getBizName()).isEqualTo("冲刺小班");
    }

    @Test
    void submit_plannerNotFound_throws404() {
        when(plannerMapper.selectById(10L)).thenReturn(null);

        assertThatThrownBy(() -> service.submit(ConsultationBizType.PLANNER, 10L, dto()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("规划师不存在");

        verify(consultationMapper, never()).insert(any(Consultation.class));
    }

    @Test
    void submit_plannerDeleted_throws404() {
        when(plannerMapper.selectById(10L)).thenReturn(Planner.builder()
                .id(10L).userId(100L).name("张老师").deleted(true).build());

        assertThatThrownBy(() -> service.submit(ConsultationBizType.PLANNER, 10L, dto()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("规划师不存在");
    }

    @Test
    void submit_institutionOffline_throws404() {
        when(institutionMapper.selectById(20L)).thenReturn(Institution.builder()
                .id(20L).userId(200L).name("冲刺小班").status((short) 0).build());

        assertThatThrownBy(() -> service.submit(ConsultationBizType.INSTITUTION, 20L, dto()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");

        verify(memberNotificationMapper, never()).insert(any(MemberNotification.class));
    }

    @Test
    void submit_teacherRevoked_throws400() {
        when(plannerMapper.selectById(10L)).thenReturn(Planner.builder()
                .id(10L).userId(100L).name("张老师").deleted(false).build());
        when(memberMapper.selectById(100L)).thenReturn(Member.builder()
                .id(100L).teacher(false).deleted(false).build());

        assertThatThrownBy(() -> service.submit(ConsultationBizType.PLANNER, 10L, dto()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("暂不可接收留言");

        verify(consultationMapper, never()).insert(any(Consultation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void page_filtersByCurrentTeacherAndMapsVO() {
        loginAs(100L);
        Page<Consultation> page = new Page<>(1, 10);
        page.setRecords(List.of(consultation(1L, 100L, ConsultationStatus.PENDING)));
        page.setTotal(1);
        when(consultationMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        ConsultationQueryDTO dto = new ConsultationQueryDTO();
        dto.setStatus(0);
        dto.setBizType("planner");
        IPage<ConsultationListVO> result = service.page(dto);

        ConsultationListVO vo = result.getRecords().get(0);
        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getBizType()).isEqualTo("planner");
        assertThat(vo.getStatus()).isEqualTo(0);
        assertThat(vo.getBizName()).isEqualTo("张老师");

        ArgumentCaptor<Wrapper<Consultation>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(consultationMapper).selectPage(any(Page.class), wrapperCaptor.capture());
        LambdaQueryWrapper<Consultation> wrapper =
                (LambdaQueryWrapper<Consultation>) wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("teacher_member_id", "status", "biz_type");
        assertThat(wrapper.getParamNameValuePairs().values())
                .contains(100L, ConsultationStatus.PENDING, ConsultationBizType.PLANNER);
    }

    @Test
    void page_invalidStatus_throws400() {
        loginAs(100L);
        ConsultationQueryDTO dto = new ConsultationQueryDTO();
        dto.setStatus(9);

        assertThatThrownBy(() -> service.page(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态值不合法");

        verify(consultationMapper, never()).selectPage(any(), any());
    }

    @Test
    void page_invalidBizType_throws400() {
        loginAs(100L);
        ConsultationQueryDTO dto = new ConsultationQueryDTO();
        dto.setBizType("unknown");

        assertThatThrownBy(() -> service.page(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("业务类型不合法");
    }

    @Test
    void detail_success() {
        loginAs(100L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation(1L, 100L, ConsultationStatus.PENDING));

        ConsultationDetailVO vo = service.detail(1L);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getMessage()).isEqualTo("想咨询高考志愿");
        assertThat(vo.getPhone()).isEqualTo("13800000000");
        assertThat(vo.getStatusDesc()).isEqualTo("待处理");
    }

    @Test
    void detail_notOwner_throws404() {
        loginAs(999L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation(1L, 100L, ConsultationStatus.PENDING));

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("留言不存在");
    }

    @Test
    void markHandled_pending_updatesConditionally() {
        loginAs(100L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation(1L, 100L, ConsultationStatus.PENDING));

        service.markHandled(1L);

        verify(consultationMapper).markHandled(eq(1L), eq(100L), any(OffsetDateTime.class));
    }

    @Test
    void markHandled_alreadyHandled_isIdempotent() {
        loginAs(100L);
        when(consultationMapper.selectById(1L)).thenReturn(consultation(1L, 100L, ConsultationStatus.HANDLED));

        service.markHandled(1L);

        verify(consultationMapper, never()).markHandled(anyLong(), anyLong(), any());
    }

    @Test
    void unhandledCount_returnsCount() {
        loginAs(100L);
        when(consultationMapper.selectCount(any())).thenReturn(3L);

        assertThat(service.unhandledCount().getUnhandledCount()).isEqualTo(3);
    }
}
