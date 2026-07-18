package com.haifeng.admin.service.impl.employment.contentManagement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeUpdateDTO;
import com.haifeng.admin.service.employment.contentManagement.NoticeService;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeListVO;
import com.haifeng.common.entity.employment.contentManagement.Notice;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.NoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public IPage<NoticeListVO> page(NoticeQueryDTO dto) {
        Page<Notice> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getIsDeleted, false);

        if (StringUtils.hasText(dto.getNoticeCategory())) {
            wrapper.eq(Notice::getNoticeCategory, dto.getNoticeCategory());
        }
        if (StringUtils.hasText(dto.getNoticeType())) {
            wrapper.eq(Notice::getNoticeType, dto.getNoticeType());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(Notice::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(Notice::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getYear())) {
            wrapper.eq(Notice::getYear, dto.getYear());
        }
        if (dto.getIsTop() != null) {
            wrapper.eq(Notice::getIsTop, dto.getIsTop());
        }
        if (dto.getIsImportant() != null) {
            wrapper.eq(Notice::getIsImportant, dto.getIsImportant());
        }
        if (StringUtils.hasText(dto.getTitle())) {
            wrapper.like(Notice::getTitle, dto.getTitle());
        }

        wrapper.orderByDesc(Notice::getSortOrder).orderByDesc(Notice::getCreatedAt);

        IPage<Notice> noticePage = noticeMapper.selectPage(page, wrapper);

        return noticePage.convert(notice -> {
            NoticeListVO vo = new NoticeListVO();
            BeanUtils.copyProperties(notice, vo);
            return vo;
        });
    }

    @Override
    public NoticeDetailVO detail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }
        NoticeDetailVO vo = new NoticeDetailVO();
        BeanUtils.copyProperties(notice, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, NoticeUpdateDTO dto) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }
        if (dto.getNoticeCategory() != null) notice.setNoticeCategory(dto.getNoticeCategory());
        if (dto.getNoticeType() != null) notice.setNoticeType(dto.getNoticeType());
        if (dto.getTitle() != null) notice.setTitle(dto.getTitle());
        if (dto.getSummary() != null) notice.setSummary(dto.getSummary());
        if (dto.getContent() != null) notice.setContent(dto.getContent());
        if (dto.getProvince() != null) notice.setProvince(dto.getProvince());
        if (dto.getCity() != null) notice.setCity(dto.getCity());
        if (dto.getTags() != null) notice.setTags(dto.getTags());
        if (dto.getYear() != null) notice.setYear(dto.getYear());
        if (dto.getSource() != null) notice.setSource(dto.getSource());
        if (dto.getSourceUrl() != null) notice.setSourceUrl(dto.getSourceUrl());
        if (dto.getPublishDate() != null) notice.setPublishDate(dto.getPublishDate());
        if (dto.getPublishUnit() != null) notice.setPublishUnit(dto.getPublishUnit());
        if (dto.getRegStartDate() != null) notice.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) notice.setRegEndDate(dto.getRegEndDate());
        if (dto.getExamTime() != null) notice.setExamTime(dto.getExamTime());
        if (dto.getRecruitmentCount() != null) notice.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getIsTop() != null) notice.setIsTop(dto.getIsTop());
        if (dto.getIsImportant() != null) notice.setIsImportant(dto.getIsImportant());
        if (dto.getSortOrder() != null) notice.setSortOrder(dto.getSortOrder());
        noticeMapper.updateById(notice);
        log.info("更新公告成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }
        notice.setIsDeleted(true);
        noticeMapper.updateById(notice);
        log.info("软删除公告成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        notice.setIsDeleted(status == 0);
        noticeMapper.updateById(notice);
        log.info("更新公告状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        noticeMapper.update(null,
                Wrappers.lambdaUpdate(Notice.class)
                        .set(Notice::getIsDeleted, true)
                        .in(Notice::getId, ids));
        log.info("批量删除公告成功: count={}", ids.size());
    }

}
