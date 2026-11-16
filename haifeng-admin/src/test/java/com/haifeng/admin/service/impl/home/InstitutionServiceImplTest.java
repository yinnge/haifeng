package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.InstitutionAddDTO;
import com.haifeng.admin.dto.home.InstitutionQueryDTO;
import com.haifeng.admin.dto.home.InstitutionUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.vo.home.InstitutionDetailVO;
import com.haifeng.admin.vo.home.InstitutionListVO;
import com.haifeng.admin.vo.home.InstitutionOptionVO;
import com.haifeng.admin.vo.home.TeacherOptionVO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.entity.home.InstitutionClassType;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @Mock
    private InstitutionMapper institutionMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private InstitutionServiceImpl service;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        // 单测没有 Spring 容器，LambdaQueryWrapper 需要手动初始化实体元数据
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Institution.class);
        TableInfoHelper.initTableInfo(assistant, Member.class);
    }

    private static InstitutionAddDTO addDto() {
        InstitutionAddDTO dto = new InstitutionAddDTO();
        dto.setCategory("高中冲刺小班");
        dto.setUserId(100L);
        dto.setName("机构A");
        dto.setProvince("北京");
        dto.setWechatQrcodeUrl("/static/qr/a.png");
        dto.setCarouselImages(List.of("/static/img/a.png"));
        dto.setClassTypes(List.of(InstitutionClassType.builder()
                .title("艺考文化课 90 天集训")
                .introduction("联考后开班")
                .tag("90 天")
                .build()));
        return dto;
    }

    private static Member teacherMember(Long id) {
        return Member.builder().id(id).username("teacher1").phone("13800000000").teacher(true).build();
    }

    @Test
    void add_invalidCategory_throws400() {
        InstitutionAddDTO dto = addDto();
        dto.setCategory("不存在的分类");

        assertThatThrownBy(() -> service.add(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("机构分类不合法");

        verify(institutionMapper, never()).insert(any(Institution.class));
    }

    @Test
    void add_invalidProvince_throws400() {
        InstitutionAddDTO dto = addDto();
        dto.setProvince("火星");

        assertThatThrownBy(() -> service.add(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("省份不合法");
    }

    @Test
    void add_memberNotFound_throws400() {
        when(memberMapper.selectById(100L)).thenReturn(null);

        assertThatThrownBy(() -> service.add(addDto()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("管理账号不存在");
    }

    @Test
    void add_memberNotTeacher_throws400() {
        Member member = Member.builder().id(100L).teacher(false).build();
        when(memberMapper.selectById(100L)).thenReturn(member);

        assertThatThrownBy(() -> service.add(addDto()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("管理账号必须是老师身份");
    }

    @Test
    void add_memberAlreadyBound_throws400() {
        when(memberMapper.selectById(100L)).thenReturn(teacherMember(100L));
        when(institutionMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.add(addDto()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已绑定其他机构");

        verify(institutionMapper, never()).insert(any(Institution.class));
    }

    @Test
    void add_success_setsDefaultsAndNormalizes() {
        InstitutionAddDTO dto = addDto();
        dto.setPhone(" ");
        when(memberMapper.selectById(100L)).thenReturn(teacherMember(100L));
        when(institutionMapper.selectCount(any())).thenReturn(0L);

        service.add(dto);

        ArgumentCaptor<Institution> captor = ArgumentCaptor.forClass(Institution.class);
        verify(institutionMapper).insert(captor.capture());
        Institution saved = captor.getValue();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCategory()).isEqualTo("高中冲刺小班");
        assertThat(saved.getProvince()).isEqualTo("北京");
        assertThat(saved.getStatus()).isEqualTo((short) 1);
        assertThat(saved.getSortOrder()).isZero();
        assertThat(saved.getVersion()).isZero();
        assertThat(saved.getPhone()).isNull();
        assertThat(saved.getCarouselImages()).containsExactly("/static/img/a.png");
        assertThat(saved.getClassTypes()).hasSize(1);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void add_duplicateKey_throws400() {
        when(memberMapper.selectById(100L)).thenReturn(teacherMember(100L));
        when(institutionMapper.selectCount(any())).thenReturn(0L);
        when(institutionMapper.insert(any(Institution.class))).thenThrow(new DuplicateKeyException("uk_institutions_user"));

        assertThatThrownBy(() -> service.add(addDto()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已绑定其他机构");
    }

    @Test
    void update_notFound_throws404() {
        when(institutionMapper.selectById(1L)).thenReturn(null);

        InstitutionUpdateDTO dto = new InstitutionUpdateDTO();
        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }

    @Test
    void update_memberBoundToOtherInstitution_throws400() {
        Institution existing = Institution.builder().id(1L).userId(100L).version(0).build();
        when(institutionMapper.selectById(1L)).thenReturn(existing);
        when(memberMapper.selectById(100L)).thenReturn(teacherMember(100L));
        when(institutionMapper.selectCount(any())).thenReturn(1L);

        InstitutionUpdateDTO dto = new InstitutionUpdateDTO();
        dto.setCategory("考公考编机构");
        dto.setUserId(100L);
        dto.setName("机构A");
        dto.setProvince("北京");

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已绑定其他机构");

        verify(institutionMapper, never()).updateById(any(Institution.class));
    }

    @Test
    void update_success_overwritesAllFields() {
        Institution existing = Institution.builder().id(1L).userId(100L).version(0)
                .name("旧名称").category("高中冲刺小班").province("北京").build();
        when(institutionMapper.selectById(1L)).thenReturn(existing);
        when(memberMapper.selectById(200L)).thenReturn(teacherMember(200L));
        when(institutionMapper.selectCount(any())).thenReturn(0L);
        when(institutionMapper.updateById(any(Institution.class))).thenReturn(1);

        InstitutionUpdateDTO dto = new InstitutionUpdateDTO();
        dto.setCategory("职业培训机构");
        dto.setUserId(200L);
        dto.setName("新名称");
        dto.setProvince("上海");
        dto.setSortOrder(null);

        service.update(1L, dto);

        assertThat(existing.getCategory()).isEqualTo("职业培训机构");
        assertThat(existing.getUserId()).isEqualTo(200L);
        assertThat(existing.getName()).isEqualTo("新名称");
        assertThat(existing.getProvince()).isEqualTo("上海");
        assertThat(existing.getSortOrder()).isZero();
        assertThat(existing.getCarouselImages()).isEmpty();
        assertThat(existing.getClassTypes()).isEmpty();
        verify(institutionMapper).updateById(existing);
    }

    @Test
    void update_optimisticLockConflict_throws400() {
        Institution existing = Institution.builder().id(1L).userId(100L).version(0).build();
        when(institutionMapper.selectById(1L)).thenReturn(existing);
        when(memberMapper.selectById(100L)).thenReturn(teacherMember(100L));
        when(institutionMapper.selectCount(any())).thenReturn(0L);
        when(institutionMapper.updateById(any(Institution.class))).thenReturn(0);

        InstitutionUpdateDTO dto = new InstitutionUpdateDTO();
        dto.setCategory("高中冲刺小班");
        dto.setUserId(100L);
        dto.setName("机构A");
        dto.setProvince("北京");

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已被他人修改");
    }

    @Test
    void updateStatus_success() {
        Institution existing = Institution.builder().id(1L).status((short) 1).version(0).build();
        when(institutionMapper.selectById(1L)).thenReturn(existing);
        when(institutionMapper.updateById(any(Institution.class))).thenReturn(1);

        StatusDTO dto = new StatusDTO();
        dto.setStatus((short) 0);

        service.updateStatus(1L, dto);

        assertThat(existing.getStatus()).isZero();
        verify(institutionMapper).updateById(existing);
    }

    @Test
    void delete_success_physicalDelete() {
        when(institutionMapper.selectById(1L)).thenReturn(Institution.builder().id(1L).build());

        service.delete(1L);

        verify(institutionMapper).deleteById(1L);
    }

    @Test
    void page_mapsManagerInfoAndFilters() {
        Institution entity = Institution.builder()
                .id(1L).name("机构A").category("高中冲刺小班").province("北京")
                .userId(100L).status((short) 1).sortOrder(0).build();
        Page<Institution> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(institutionMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        when(memberMapper.selectBatchIds(any())).thenReturn(List.of(teacherMember(100L)));

        InstitutionQueryDTO dto = new InstitutionQueryDTO();
        dto.setName("机构");
        dto.setCategory("高中冲刺小班");
        dto.setProvince("北京");

        IPage<InstitutionListVO> result = service.page(dto);

        assertThat(result.getTotal()).isEqualTo(1);
        InstitutionListVO vo = result.getRecords().get(0);
        assertThat(vo.getManagerUsername()).isEqualTo("teacher1");
        assertThat(vo.getManagerPhone()).isEqualTo("13800000000");
        assertThat(vo.getCategory()).isEqualTo("高中冲刺小班");
    }

    @Test
    void detail_mapsAllFieldsAndManager() {
        Institution entity = Institution.builder()
                .id(1L).name("机构A").category("高中冲刺小班").province("北京")
                .userId(100L).status((short) 1)
                .carouselImages(List.of("/static/img/a.png"))
                .classTypes(List.of(InstitutionClassType.builder().title("班型A").build()))
                .build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);
        when(memberMapper.selectById(100L)).thenReturn(teacherMember(100L));

        InstitutionDetailVO vo = service.detail(1L);

        assertThat(vo.getCarouselImages()).containsExactly("/static/img/a.png");
        assertThat(vo.getClassTypes()).hasSize(1);
        assertThat(vo.getManagerUsername()).isEqualTo("teacher1");
        assertThat(vo.getManagerPhone()).isEqualTo("13800000000");
    }

    @Test
    void categories_returnsFourFixedOptions() {
        List<InstitutionOptionVO> options = service.categories();

        assertThat(options).hasSize(4);
        assertThat(options).extracting(InstitutionOptionVO::getValue)
                .containsExactly("高中冲刺小班", "考公考编机构", "央国企培训", "职业培训机构");
    }

    @Test
    void teacherOptions_mapsIdAndUsernameOnly() {
        when(memberMapper.selectList(any())).thenReturn(List.of(
                Member.builder().id(100L).username("teacher1").teacher(true).build(),
                Member.builder().id(200L).username("teacher2").teacher(true).build()));

        List<TeacherOptionVO> options = service.listTeacherOptions();

        assertThat(options).hasSize(2);
        assertThat(options.get(0).getId()).isEqualTo(100L);
        assertThat(options.get(0).getUsername()).isEqualTo("teacher1");
        assertThat(options.get(1).getId()).isEqualTo(200L);
        assertThat(options.get(1).getUsername()).isEqualTo("teacher2");
    }
}
