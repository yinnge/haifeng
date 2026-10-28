package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.service.impl.university.UniversityServiceImpl;
import com.haifeng.app.vo.university.ChannelOptionVO;
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.app.vo.university.UniversityStatsVO;
import com.haifeng.common.entity.special.SpecialChannel;
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelMapper;
import com.haifeng.common.mapper.special.SpecialChannelUniversityMapper;
import com.haifeng.common.mapper.university.UniversityDetailMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniversityServiceImplTest {

    @Mock private UniversityMapper universityMapper;
    @Mock private UniversityDetailMapper universityDetailMapper;
    @Mock private SpecialChannelUniversityMapper specialChannelUniversityMapper;
    @Mock private SpecialChannelMapper specialChannelMapper;

    @InjectMocks private UniversityServiceImpl service;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        // MP 的 Lambda 条件列解析与参数物化依赖 TableInfo 缓存，单测环境手动初始化
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), University.class);
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), UniversityDetail.class);
    }

    /** 构建「一页空数据」的模拟返回并捕获查询条件，供断言 SQL 片段内的绑定参数。 */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private AbstractWrapper<?, ?, ?> capturePageWrapper(UniversityQueryDTO dto) {
        Page<University> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        when(universityMapper.selectPage(any(Page.class), wrapperCaptor.capture())).thenReturn(page);

        service.page(dto);

        AbstractWrapper<?, ?, ?> wrapper = (AbstractWrapper<?, ?, ?>) wrapperCaptor.getValue();
        // mapper 被 mock，SQL 不会真正落地；手动触发一次片段构建以物化绑定参数
        wrapper.getSqlSegment();
        return wrapper;
    }

    @Test
    void page_returnsConvertedVOs() {
        University entity = University.builder()
                .id(1L).name("清华大学").cityName("北京")
                .educationLevel("本科").provinceName("北京")
                .nature("公办").category("综合").majorCount(120)
                .introduction("简介内容")
                .status((short) 1).build();
        Page<University> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(universityMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        when(universityDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        UniversityQueryDTO dto = new UniversityQueryDTO();
        IPage<UniversityListVO> result = service.page(dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("清华大学");
        assertThat(result.getRecords().get(0).getMajorCount()).isEqualTo(120);
        // introduction 更名为 content 输出
        assertThat(result.getRecords().get(0).getContent()).isEqualTo("简介内容");
    }

    /** 封面图：detail 轮播非空时取首图作为 coverImage。 */
    @Test
    void page_detailWithCarousel_firstImageAsCover() {
        University entity = University.builder().id(1L).name("清华大学").status((short) 1).build();
        UniversityDetail detail = UniversityDetail.builder()
                .universityId(1L).carouselImages(List.of("http://img/1.jpg", "http://img/2.jpg"))
                .status((short) 1).build();
        Page<University> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(universityMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        when(universityDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(detail));

        UniversityQueryDTO dto = new UniversityQueryDTO();
        IPage<UniversityListVO> result = service.page(dto);

        assertThat(result.getRecords().get(0).getCoverImage()).isEqualTo("http://img/1.jpg");
    }

    /** 封面图：detail 缺失/轮播为空 → coverImage 为 null（前端占位图）。 */
    @Test
    void page_detailMissingOrEmptyCarousel_coverNull() {
        University e1 = University.builder().id(1L).name("无详情").status((short) 1).build();
        University e2 = University.builder().id(2L).name("空轮播").status((short) 1).build();
        UniversityDetail empty = UniversityDetail.builder()
                .universityId(2L).carouselImages(List.of()).status((short) 1).build();
        Page<University> page = new Page<>(1, 10);
        page.setRecords(List.of(e1, e2));
        page.setTotal(2);
        when(universityMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        when(universityDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(empty));

        UniversityQueryDTO dto = new UniversityQueryDTO();
        IPage<UniversityListVO> result = service.page(dto);

        assertThat(result.getRecords()).extracting(UniversityListVO::getCoverImage)
                .containsOnlyNulls();
    }

    /** 标签筛选：多标签逐条拼接 tags @> ARRAY[?]（AND 交集），参数绑定不内联。 */
    @Test
    void page_tagsFilter_appliesArrayContainment() {
        UniversityQueryDTO dto = new UniversityQueryDTO();
        dto.setTags(List.of(" 985 ", "211"));

        AbstractWrapper<?, ?, ?> wrapper = capturePageWrapper(dto);

        assertThat(wrapper.getParamNameValuePairs().values())
                .contains("985", "211");
        assertThat(wrapper.getSqlSegment()).contains("tags @> ARRAY");
    }

    /** 招生通道筛选：关联表查出的 universityIds 以 IN 条件进入主查询。 */
    @Test
    void page_channelCode_withUniversities_filtersIn() {
        SpecialChannelUniversity link = SpecialChannelUniversity.builder()
                .channelCode("qiangji").universityId(7L).isActive(true).build();
        when(specialChannelUniversityMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(link));

        Page<University> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        when(universityMapper.selectPage(any(Page.class), wrapperCaptor.capture())).thenReturn(page);

        UniversityQueryDTO dto = new UniversityQueryDTO();
        dto.setChannelCode("qiangji");
        service.page(dto);

        AbstractWrapper<?, ?, ?> wrapper = (AbstractWrapper<?, ?, ?>) wrapperCaptor.getValue();
        wrapper.getSqlSegment();
        assertThat(wrapper.getParamNameValuePairs().values()).contains(7L);
    }

    /** 招生通道筛选：关联表无数据 → 直接空页，不打主表查询。 */
    @Test
    void page_channelCode_noUniversities_returnsEmptyPage() {
        when(specialChannelUniversityMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        UniversityQueryDTO dto = new UniversityQueryDTO();
        dto.setChannelCode("lianzhao");
        IPage<UniversityListVO> result = service.page(dto);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isZero();
        verifyNoInteractions(universityMapper);
    }

    /** 统招约定：前端不传 channelCode → 不查关联表。 */
    @Test
    void page_noChannelCode_skipsAssociationQuery() {
        UniversityQueryDTO dto = new UniversityQueryDTO();
        capturePageWrapper(dto);

        verifyNoInteractions(specialChannelUniversityMapper);
    }

    /** 统计概览：985/211/双一流按 tags 包含独立计数，本科=本科+本专兼招。 */
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void stats_countsFourCategories() {
        when(universityMapper.selectCount(any(Wrapper.class))).thenAnswer(inv -> {
            AbstractWrapper<?, ?, ?> w = (AbstractWrapper<?, ?, ?>) inv.getArgument(0);
            String params = w.getParamNameValuePairs().values().toString();
            if (params.contains("985")) return 39L;
            if (params.contains("211")) return 112L;
            if (params.contains("双一流")) return 147L;
            return 1243L;
        });

        UniversityStatsVO vo = service.stats();

        assertThat(vo.getCount985()).isEqualTo(39L);
        assertThat(vo.getCount211()).isEqualTo(112L);
        assertThat(vo.getCountDoubleFirstClass()).isEqualTo(147L);
        assertThat(vo.getCountUndergraduate()).isEqualTo(1243L);
    }

    /** 招生类型下拉：查 t_special_channel（is_active=true，sort_order 升序）。 */
    @Test
    void listSpecialChannelOptions_returnsActiveChannels() {
        SpecialChannel c1 = SpecialChannel.builder()
                .channelCode("tongzhao").channelName("统招").sortOrder(1).isActive(true).build();
        SpecialChannel c2 = SpecialChannel.builder()
                .channelCode("qiangji").channelName("强基计划").sortOrder(2).isActive(true).build();
        when(specialChannelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1, c2));

        List<ChannelOptionVO> options = service.listSpecialChannelOptions();

        assertThat(options).hasSize(2);
        assertThat(options.get(0).getChannelCode()).isEqualTo("tongzhao");
        assertThat(options.get(1).getChannelName()).isEqualTo("强基计划");
    }

    /** 省份归一化：输入全称「天津市」也须剥成核名「天津」再与库列比较。 */
    @Test
    void page_provinceFullName_strippedToCoreName() {
        UniversityQueryDTO dto = new UniversityQueryDTO();
        dto.setProvinceName("天津市");

        AbstractWrapper<?, ?, ?> wrapper = capturePageWrapper(dto);

        assertThat(wrapper.getParamNameValuePairs().values())
                .contains("天津")
                .doesNotContain("天津市");
    }

    /** 省份归一化：前端简称（含三字的内蒙古）传参原样作为核名使用。 */
    @Test
    void page_provinceShortName_usedAsCoreName() {
        UniversityQueryDTO dto = new UniversityQueryDTO();
        dto.setProvinceName("内蒙古");

        AbstractWrapper<?, ?, ?> wrapper = capturePageWrapper(dto);

        assertThat(wrapper.getParamNameValuePairs().values()).contains("内蒙古");
    }

    /** 省份为空白串时不拼接任何省份条件。 */
    @Test
    void page_provinceBlank_noConditionApplied() {
        UniversityQueryDTO dto = new UniversityQueryDTO();
        dto.setProvinceName("   ");

        AbstractWrapper<?, ?, ?> wrapper = capturePageWrapper(dto);

        assertThat(wrapper.getParamNameValuePairs().values()).doesNotContain("");
    }

    @Test
    void page_passesPageNumberAndSizeFromDto() {
        UniversityQueryDTO dto = new UniversityQueryDTO();
        dto.setPage(3);
        dto.setSize(20);

        Page<University> page = new Page<>(3, 20);
        page.setRecords(List.of());
        page.setTotal(0);

        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        when(universityMapper.selectPage(pageCaptor.capture(), any(Wrapper.class))).thenReturn(page);

        service.page(dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(3L);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20L);
    }

    @Test
    void detail_returnsMergedVO() {
        University univ = University.builder()
                .id(1L).name("清华大学").nameEn("Tsinghua").provinceName("北京")
                .cityName("北京").region("华北").category("综合").majorCount(120)
                .educationLevel("本科").nature("公办").department("教育部")
                .hasDoctorate(true).hasMaster(true).status((short) 1).build();
        UniversityDetail detail = UniversityDetail.builder()
                .id(11L).universityId(1L).address("北京市海淀区")
                .admissionPhone("010-62770334").website("https://www.tsinghua.edu.cn")
                .introduction("详细介绍").status((short) 1).build();

        when(universityMapper.selectById(1L)).thenReturn(univ);
        when(universityDetailMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(detail);

        UniversityDetailVO vo = service.detail(1L);

        assertThat(vo.getName()).isEqualTo("清华大学");
        assertThat(vo.getAddress()).isEqualTo("北京市海淀区");
        assertThat(vo.getIntroduction()).isEqualTo("详细介绍");
    }

    @Test
    void detail_universityNotFound_throws404() {
        when(universityMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("院校不存在");
    }

    @Test
    void detail_universityStatusZero_throws404() {
        University univ = University.builder().id(1L).status((short) 0).build();
        when(universityMapper.selectById(1L)).thenReturn(univ);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("院校不存在");
    }

    @Test
    void detail_detailRecordMissing_throws404() {
        University univ = University.builder().id(1L).status((short) 1).build();
        when(universityMapper.selectById(1L)).thenReturn(univ);
        when(universityDetailMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("院校详情不存在");
    }
}
