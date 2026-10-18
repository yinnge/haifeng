package com.haifeng.app.service.impl.algorithm.article;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.haifeng.app.dto.algorithm.article.BizArticleSearchDTO;
import com.haifeng.app.vo.algorithm.article.BizArticleSearchItemVO;
import com.haifeng.common.entity.article.BizArticle;
import com.haifeng.common.mapper.article.BizArticleMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BizArticleServiceImplSearchTest {

    @Mock private BizArticleMapper bizArticleMapper;

    @InjectMocks private BizArticleServiceImpl service;

    @Captor private ArgumentCaptor<LambdaQueryWrapper<BizArticle>> wrapperCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, BizArticle.class);
    }

    private static BizArticle article(long id, String province, int sortOrder, String title) {
        return BizArticle.builder().id(id).moduleName("高考").province(province).columnName("分数线")
                .title(title).status(0).sortOrder(sortOrder).build();
    }

    private static BizArticleSearchDTO dto(String keyword, String province, Integer limit) {
        BizArticleSearchDTO d = new BizArticleSearchDTO();
        d.setKeyword(keyword);
        d.setProvince(province);
        d.setLimit(limit);
        return d;
    }

    @Test
    void searchPublished_returnsMatchedWithHighlight() {
        String title = "2026年山西省的高考分数线在线查看";
        when(bizArticleMapper.selectList(any())).thenReturn(List.of(article(1L, "山西", 5, title)));

        List<BizArticleSearchItemVO> result = service.searchPublished("高考", dto("山西高考2026分数线", null, null));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo(title);
        assertThat(result.get(0).getHighlight()).hasSize(4);
    }

    @Test
    void searchPublished_specificProvince_addsEqFilter() {
        when(bizArticleMapper.selectList(wrapperCaptor.capture())).thenReturn(List.of());

        service.searchPublished("高考", dto("分数线", "山西", null));

        // MP 3.5.7 惰性物化：getSqlSegment 前 paramNameValuePairs 为空，先触发一次（与 CityServiceImplTest 同模式）
        wrapperCaptor.getValue().getCustomSqlSegment();
        assertThat(wrapperCaptor.getValue().getParamNameValuePairs().values()).contains("山西");
    }

    @Test
    void searchPublished_nationalProvince_omitsEqFilter() {
        when(bizArticleMapper.selectList(wrapperCaptor.capture())).thenReturn(List.of());

        service.searchPublished("高考", dto("分数线", "全国", null));

        // MP 3.5.7 惰性物化：getSqlSegment 前 paramNameValuePairs 为空，先触发一次（与 CityServiceImplTest 同模式）
        wrapperCaptor.getValue().getCustomSqlSegment();
        assertThat(wrapperCaptor.getValue().getParamNameValuePairs().values()).doesNotContain("全国");
    }

    @Test
    void searchPublished_limitOne_stopsAtFirstHit() {
        when(bizArticleMapper.selectList(any())).thenReturn(List.of(
                article(1L, "山西", 9, "山西高考分数线一览"),
                article(2L, "山西", 8, "山西高考成绩查询入口")));

        List<BizArticleSearchItemVO> result = service.searchPublished("高考", dto("山西高考", null, 1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void searchPublished_unrelatedTitle_filteredOut() {
        when(bizArticleMapper.selectList(any()))
                .thenReturn(List.of(article(1L, "山西", 9, "2026年山西省的高考分数线在线查看")));

        List<BizArticleSearchItemVO> result = service.searchPublished("高考", dto("英语四六级真题", null, null));

        assertThat(result).isEmpty();
        verify(bizArticleMapper).selectList(any());
    }
}
