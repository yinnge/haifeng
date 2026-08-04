package com.haifeng.app.service.impl.employment.contentManagement.notice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.common.entity.employment.contentManagement.Notice;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.NoticeMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public List<NoticeDetailVO> listByCategoryAndType(String noticeCategory, String noticeType) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getIsDeleted, false);
        wrapper.eq(Notice::getNoticeCategory, noticeCategory);
        wrapper.eq(StrUtil.isNotBlank(noticeType), Notice::getNoticeType, noticeType);
        wrapper.orderByDesc(Notice::getSortOrder, Notice::getCreatedAt);

        List<Notice> list = noticeMapper.selectList(wrapper);
        return list.stream().map(this::convertToDetailVO).toList();
    }

    @Override
    public IPage<NoticeDetailVO> page(NoticeQueryDTO dto) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getIsDeleted, false);
        wrapper.eq(StrUtil.isNotBlank(dto.getNoticeCategory()), Notice::getNoticeCategory, dto.getNoticeCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getNoticeType()), Notice::getNoticeType, dto.getNoticeType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), Notice::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), Notice::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getYear()), Notice::getYear, dto.getYear());

        // 关键词搜索：前端把 keyword 同时映射为 title / summary / source，三者 OR 匹配
        if (StrUtil.isNotBlank(dto.getTitle()) || StrUtil.isNotBlank(dto.getSummary()) || StrUtil.isNotBlank(dto.getSource())) {
            wrapper.and(w -> {
                if (StrUtil.isNotBlank(dto.getTitle())) {
                    w.like(Notice::getTitle, dto.getTitle());
                }
                if (StrUtil.isNotBlank(dto.getSummary())) {
                    w.or().like(Notice::getSummary, dto.getSummary());
                }
                if (StrUtil.isNotBlank(dto.getSource())) {
                    w.or().like(Notice::getSource, dto.getSource());
                }
            });
        }

        wrapper.last("ORDER BY sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        Page<Notice> page = new Page<>(dto.getPage(), dto.getSize());
        noticeMapper.selectPage(page, wrapper);

        return page.convert(this::convertToDetailVO);
    }

    @Override
    public NoticeDetailVO detail(Long id) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getId, id);
        wrapper.eq(Notice::getIsDeleted, false);
        Notice notice = noticeMapper.selectOne(wrapper);
        if (notice == null) {
            log.warn("公告不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToDetailVO(notice);
    }

    private NoticeDetailVO convertToDetailVO(Notice notice) {
        return NoticeDetailVO.builder()
                .id(notice.getId())
                .noticeCategory(notice.getNoticeCategory())
                .noticeType(notice.getNoticeType())
                .title(notice.getTitle())
                .summary(notice.getSummary())
                .content(notice.getContent())
                .province(notice.getProvince())
                .city(notice.getCity())
                .tags(notice.getTags())
                .year(notice.getYear())
                .source(notice.getSource())
                .sourceUrl(notice.getSourceUrl())
                .publishDate(notice.getPublishDate())
                .publishUnit(notice.getPublishUnit())
                .regStartDate(notice.getRegStartDate())
                .regEndDate(notice.getRegEndDate())
                .examTime(notice.getExamTime())
                .recruitmentCount(notice.getRecruitmentCount())
                .sortOrder(notice.getSortOrder())
                .isTop(notice.getIsTop())
                .isImportant(notice.getIsImportant())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
