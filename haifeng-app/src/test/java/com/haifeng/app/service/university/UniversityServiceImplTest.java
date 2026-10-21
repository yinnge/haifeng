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
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityDetail;
import com.haifeng.common.exception.BusinessException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniversityServiceImplTest {

    @Mock private UniversityMapper universityMapper;
    @Mock private UniversityDetailMapper universityDetailMapper;

    @InjectMocks private UniversityServiceImpl service;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        // MP 的 Lambda 条件列解析与参数物化依赖 TableInfo 缓存，单测环境手动初始化
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), University.class);
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
                .hasDoctorate(true).hasMaster(true).department("教育部")
                .status((short) 1).build();
        Page<University> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(universityMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        UniversityQueryDTO dto = new UniversityQueryDTO();
        IPage<UniversityListVO> result = service.page(dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("清华大学");
        assertThat(result.getRecords().get(0).getMajorCount()).isEqualTo(120);
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
