package com.haifeng.app.service.competition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.impl.competition.CompetitionServiceImpl;
import com.haifeng.app.vo.competition.CompetitionDetailVO;
import com.haifeng.app.vo.competition.CompetitionListVO;
import com.haifeng.app.vo.competition.CompetitionMajorBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionDetailMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import com.haifeng.common.response.ResultCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceImplTest {

    @Mock
    private CompetitionMapper competitionMapper;

    @Mock
    private CompetitionDetailMapper competitionDetailMapper;

    @Mock
    private CompetitionMajorMapper competitionMajorMapper;

    @InjectMocks
    private CompetitionServiceImpl service;

    @Test
    void page_ShouldReturnPagedList() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);

        Competition entity = Competition.builder()
                .id(1L).compName("蓝桥杯").compLevel("国家级").registrationTime("上半年")
                .isDeleted(false)
                .build();
        Page<Competition> mybatisPage = new Page<>(1, 10);
        mybatisPage.setRecords(List.of(entity)).setTotal(1);
        when(competitionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mybatisPage);

        IPage<CompetitionListVO> result = service.page(dto);
        assertEquals(1, result.getTotal());
        assertEquals("蓝桥杯", result.getRecords().get(0).getCompName());
    }

    @Test
    void detail_CompetitionNotFound_ShouldThrow() {
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.detail(999L));
        assertEquals(ResultCode.NOT_FOUND.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("竞赛"));
    }

    @Test
    void detail_DetailNull_ShouldReturnVOWithBaseFields() {
        Competition competition = Competition.builder()
                .id(1L).compName("蓝桥杯").isDeleted(false)
                .build();
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(competition);
        when(competitionDetailMapper.findActiveByCompetitionId(1L)).thenReturn(null);

        CompetitionDetailVO vo = service.detail(1L);
        assertEquals(1L, vo.getId());
        assertEquals(1L, vo.getCompetitionId());
        assertNull(vo.getBasicInfo());
        assertNull(vo.getAwards());
    }

    @Test
    void detail_Found_ShouldReturnFullVO() {
        Competition competition = Competition.builder()
                .id(1L).compName("蓝桥杯").isDeleted(false)
                .build();
        CompetitionDetail detail = CompetitionDetail.builder()
                .id(10L).competitionId(1L)
                .basicInfo(Map.of("organizer", "工信部", "year", 2024))
                .awards(List.of("一等奖", "二等奖"))
                .background("背景介绍")
                .purposes(List.of("促进教学", "培养人才"))
                .competitionRules(List.of(Map.of("title", "组队", "content", "3人一组")))
                .scoringCriteria(List.of("代码40%", "答辩60%"))
                .notices(List.of("需提前注册"))
                .processGuide(List.of(Map.of("step", "1", "desc", "报名")))
                .awardsDisplay(List.of(Map.of("level", "国一", "count", "10")))
                .isDeleted(false)
                .build();
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(competition);
        when(competitionDetailMapper.findActiveByCompetitionId(1L)).thenReturn(detail);

        CompetitionDetailVO vo = service.detail(1L);
        assertEquals("工信部", vo.getBasicInfo().get("organizer"));
        assertEquals(2, vo.getAwards().size());
        assertEquals("背景介绍", vo.getBackground());
        assertEquals(1, vo.getCompetitionRules().size());
    }

    @Test
    void majors_CompetitionNotFound_ShouldThrow() {
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.majors(999L, new BasePageQueryDTO()));
        assertEquals(ResultCode.NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void majors_NoRelation_ShouldReturnEmptyPage() {
        Competition competition = Competition.builder()
                .id(1L).compName("蓝桥杯").isDeleted(false)
                .build();
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(competition);
        when(competitionMajorMapper.selectMajorsByCompetitionId(any(Page.class), eq(1L)))
                .thenReturn(new Page<Map<String, Object>>(1, 10));

        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        IPage<CompetitionMajorBriefVO> result = service.majors(1L, dto);
        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void majors_Found_ShouldReturnPagedVO() {
        Competition competition = Competition.builder()
                .id(1L).compName("蓝桥杯").isDeleted(false)
                .build();
        Map<String, Object> row = Map.of("majorId", 100L, "majorName", "计算机科学");
        Page<Map<String, Object>> mybatisPage = new Page<>(1, 10);
        mybatisPage.setRecords(List.of(row)).setTotal(1);
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(competition);
        when(competitionMajorMapper.selectMajorsByCompetitionId(any(Page.class), eq(1L)))
                .thenReturn(mybatisPage);

        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        IPage<CompetitionMajorBriefVO> result = service.majors(1L, dto);
        assertEquals(1, result.getTotal());
        assertEquals(100L, result.getRecords().get(0).getMajorId());
        assertEquals("计算机科学", result.getRecords().get(0).getMajorName());
    }
}
