package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.service.impl.home.InstitutionServiceImpl;
import com.haifeng.app.vo.home.InstitutionAppointmentVO;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.entity.home.InstitutionClassType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @Mock private InstitutionMapper institutionMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks private InstitutionServiceImpl service;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        // 单测没有 Spring 容器，LambdaQueryWrapper 的 select/eq 需要手动初始化实体元数据
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), Institution.class);
    }

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void list_cacheHit_returnsDirectly() {
        InstitutionListVO vo = new InstitutionListVO();
        vo.setId(1L);
        vo.setName("cached");
        PageCacheDTO<InstitutionListVO> cached =
                new PageCacheDTO<>(List.of(vo), 1L, 1L, 10L);
        when(valueOps.get(anyString())).thenReturn(cached);

        IPage<InstitutionListVO> result = service.page(new InstitutionQueryDTO());

        assertThat(result.getRecords().get(0).getName()).isEqualTo("cached");
        verify(institutionMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_cacheMiss_queriesDbAndWritesCache() {
        String key = RedisKeyConstant.getInstitutionListKey(1, 10, null, null, null);
        when(valueOps.get(key)).thenReturn(null);

        Institution entity = Institution.builder()
                .id(1L).name("机构A").category("职业培训机构").province("北京").status((short) 1).build();
        Page<Institution> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(institutionMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        IPage<InstitutionListVO> result = service.page(new InstitutionQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void list_withName_usesLikeQuery() {
        String name = "海峰";
        String key = RedisKeyConstant.getInstitutionListKey(1, 10, name, null, null);
        when(valueOps.get(key)).thenReturn(null);

        Institution entity = Institution.builder()
                .id(1L).name("海峰志愿规划培训中心").category("职业培训机构").status((short) 1).build();
        Page<Institution> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(institutionMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        InstitutionQueryDTO dto = new InstitutionQueryDTO();
        dto.setName(name);
        IPage<InstitutionListVO> result = service.page(dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("海峰志愿规划培训中心");
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @SuppressWarnings("unchecked")
    void list_filtersByCategoryAndProvince() {
        String key = RedisKeyConstant.getInstitutionListKey(1, 10, null, "高中冲刺小班", "北京");
        when(valueOps.get(key)).thenReturn(null);

        Page<Institution> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(institutionMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        InstitutionQueryDTO dto = new InstitutionQueryDTO();
        dto.setCategory("高中冲刺小班");
        dto.setProvince("北京");
        service.page(dto);

        ArgumentCaptor<Wrapper<Institution>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(institutionMapper).selectPage(any(Page.class), captor.capture());
        LambdaQueryWrapper<Institution> wrapper = (LambdaQueryWrapper<Institution>) captor.getValue();

        assertThat(wrapper.getSqlSegment()).contains("category", "province");
        assertThat(wrapper.getParamNameValuePairs()).containsValues("高中冲刺小班", "北京");
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void list_invalidCategory_throws400() {
        InstitutionQueryDTO dto = new InstitutionQueryDTO();
        dto.setCategory("不存在的分类");

        assertThatThrownBy(() -> service.page(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("机构分类不合法");

        verify(institutionMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_invalidProvince_throws400() {
        InstitutionQueryDTO dto = new InstitutionQueryDTO();
        dto.setProvince("火星");

        assertThatThrownBy(() -> service.page(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("省份不合法");

        verify(institutionMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_mapsLeanFields() {
        String key = RedisKeyConstant.getInstitutionListKey(1, 10, null, null, null);
        when(valueOps.get(key)).thenReturn(null);

        Institution entity = Institution.builder()
                .id(1L).name("机构A").category("高中冲刺小班").province("北京")
                .tags(List.of("小班教学", "艺考文化课"))
                .status((short) 1).build();
        Page<Institution> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(institutionMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        InstitutionListVO vo = service.page(new InstitutionQueryDTO()).getRecords().get(0);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getName()).isEqualTo("机构A");
        assertThat(vo.getCategory()).isEqualTo("高中冲刺小班");
        assertThat(vo.getProvince()).isEqualTo("北京");
        assertThat(vo.getTags()).containsExactly("小班教学", "艺考文化课");
    }

    @Test
    void detail_cacheHit_returnsDirectly() {
        InstitutionDetailVO cached = new InstitutionDetailVO();
        cached.setId(1L);
        cached.setName("cached");
        when(valueOps.get(RedisKeyConstant.getInstitutionDetailKey(1L))).thenReturn(cached);

        InstitutionDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("cached");
        verify(institutionMapper, never()).selectById(anyLong());
    }

    @Test
    void detail_cacheMissAndExists_queriesDbAndCaches() {
        String key = RedisKeyConstant.getInstitutionDetailKey(1L);
        when(valueOps.get(key)).thenReturn(null);
        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 1).build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        InstitutionDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("机构A");
        verify(valueOps).set(eq(key), any(InstitutionDetailVO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_notFound_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(institutionMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }

    @Test
    void detail_statusZero_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 0).build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }

    @Test
    void detail_returnsIdToAdvantages() {
        String key = RedisKeyConstant.getInstitutionDetailKey(1L);
        when(valueOps.get(key)).thenReturn(null);

        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 1)
                .category("高中冲刺小班").province("北京").foundedYear("2013年")
                .carouselImages(List.of("/img/a.png"))
                .tags(List.of("小班教学"))
                .introduction("介绍内容")
                .mainDirection("美术")
                .classSize("15人以内")
                .teacherConfig("全职教师")
                .courseForm("联考后全日制")
                .classTypes(List.of(InstitutionClassType.builder()
                        .title("艺考文化课 90 天集训").introduction("联考后开班").tag("90 天").build()))
                .notice("温馨提示")
                .advantages(List.of("师资强", "管理严"))
                // 联系方式不在详情接口返回
                .address("北京市").wechatQrcodeUrl("/static/qr/a.png")
                .email("a@b.com").phone("13800000000")
                .build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        InstitutionDetailVO vo = service.detail(1L);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getCategory()).isEqualTo("高中冲刺小班");
        assertThat(vo.getName()).isEqualTo("机构A");
        assertThat(vo.getProvince()).isEqualTo("北京");
        assertThat(vo.getFoundedYear()).isEqualTo("2013年");
        assertThat(vo.getCarouselImages()).containsExactly("/img/a.png");
        assertThat(vo.getTags()).containsExactly("小班教学");
        assertThat(vo.getIntroduction()).isEqualTo("介绍内容");
        assertThat(vo.getMainDirection()).isEqualTo("美术");
        assertThat(vo.getClassSize()).isEqualTo("15人以内");
        assertThat(vo.getTeacherConfig()).isEqualTo("全职教师");
        assertThat(vo.getCourseForm()).isEqualTo("联考后全日制");
        assertThat(vo.getClassTypes()).hasSize(1);
        assertThat(vo.getClassTypes().get(0).getTitle()).isEqualTo("艺考文化课 90 天集训");
        assertThat(vo.getNotice()).isEqualTo("温馨提示");
        assertThat(vo.getAdvantages()).containsExactly("师资强", "管理严");
    }

    @Test
    void appointment_mapsNameAndContacts() {
        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 1)
                .address("北京市").wechatQrcodeUrl("/static/qr/a.png")
                .email("a@b.com").phone("13800000000")
                .build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        InstitutionAppointmentVO vo = service.appointment(1L);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getName()).isEqualTo("机构A");
        assertThat(vo.getAddress()).isEqualTo("北京市");
        assertThat(vo.getWechatQrcodeUrl()).isEqualTo("/static/qr/a.png");
        assertThat(vo.getEmail()).isEqualTo("a@b.com");
        assertThat(vo.getPhone()).isEqualTo("13800000000");
        // 联系方式直连 DB，不写缓存
        verify(valueOps, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void appointment_notFound_throws404() {
        when(institutionMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.appointment(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }

    @Test
    void appointment_statusZero_throws404() {
        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 0).build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> service.appointment(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }
}
