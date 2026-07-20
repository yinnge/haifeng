package com.haifeng.app.service.impl.employment.contentManagement.notice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.common.entity.employment.contentManagement.Notice;
import com.haifeng.common.mapper.employment.contentManagement.NoticeMapper;
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
        wrapper.orderBy(true, false, "sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        List<Notice> list = noticeMapper.selectList(wrapper);
        return list.stream().map(this::convertToDetailVO).toList();
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
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
