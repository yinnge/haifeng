package com.haifeng.app.service.impl.employment.contentManagement.notice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeListVO;
import com.haifeng.common.entity.employment.contentManagement.Notice;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.NoticeMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public IPage<NoticeListVO> page(NoticeQueryDTO dto) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getTitle()) || StrUtil.isNotBlank(dto.getSummary()) || StrUtil.isNotBlank(dto.getSource())) {
            wrapper.and(w -> {
                boolean hasPrev = false;
                if (StrUtil.isNotBlank(dto.getTitle())) {
                    w.like(Notice::getTitle, dto.getTitle());
                    hasPrev = true;
                }
                if (StrUtil.isNotBlank(dto.getSummary())) {
                    if (hasPrev) { w.or(); }
                    w.like(Notice::getSummary, dto.getSummary());
                    hasPrev = true;
                }
                if (StrUtil.isNotBlank(dto.getSource())) {
                    if (hasPrev) { w.or(); }
                    w.like(Notice::getSource, dto.getSource());
                }
            });
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getNoticeCategory()), Notice::getNoticeCategory, dto.getNoticeCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getNoticeType()), Notice::getNoticeType, dto.getNoticeType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), Notice::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), Notice::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getYear()), Notice::getYear, dto.getYear());

        wrapper.orderByDesc(Notice::getIsTop);
        wrapper.orderByDesc(Notice::getPublishDate).last("NULLS LAST");

        Page<Notice> page = new Page<>(dto.getPage(), dto.getSize());
        noticeMapper.selectPage(page, wrapper);

        return page.convert(notice -> NoticeListVO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .summary(notice.getSummary())
                .publishDate(notice.getPublishDate())
                .viewCount(notice.getViewCount())
                .noticeCategory(notice.getNoticeCategory())
                .province(notice.getProvince())
                .city(notice.getCity())
                .year(notice.getYear())
                .regStartDate(notice.getRegStartDate())
                .regEndDate(notice.getRegEndDate())
                .recruitmentCount(notice.getRecruitmentCount())
                .build());
    }

    @Override
    public NoticeDetailVO detail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || Boolean.TRUE.equals(notice.getIsDeleted())) {
            log.warn("公告不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
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
                .isTop(notice.getIsTop())
                .isImportant(notice.getIsImportant())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
