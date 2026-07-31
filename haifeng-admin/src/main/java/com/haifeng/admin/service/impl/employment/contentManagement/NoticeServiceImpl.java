package com.haifeng.admin.service.impl.employment.contentManagement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeAddDTO;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeUpdateDTO;
import com.haifeng.admin.service.employment.contentManagement.NoticeService;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeListVO;
import com.haifeng.common.entity.employment.contentManagement.Notice;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.NoticeMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public IPage<NoticeListVO> page(NoticeQueryDTO dto) {
        Page<Notice> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Notice> noticePage = noticeMapper.selectNoticePage(page,
                dto.getTitle(), dto.getNoticeCategory(), dto.getNoticeType(), dto.getProvince(), dto.getCity(),
                dto.getYear(), dto.getIsTop(), dto.getIsImportant(), dto.getStatus());

        return noticePage.convert(notice -> {
            NoticeListVO vo = new NoticeListVO();
            BeanUtils.copyProperties(notice, vo);
            return vo;
        });
    }

    @Override
    public NoticeDetailVO detail(Long id) {
        Notice notice = noticeMapper.selectNoticeById(id);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        NoticeDetailVO vo = new NoticeDetailVO();
        BeanUtils.copyProperties(notice, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(NoticeAddDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
        Notice entity = Notice.builder()
                .id(SnowflakeIdGenerator.nextId())
                .noticeCategory(dto.getNoticeCategory())
                .noticeType(dto.getNoticeType())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .content(dto.getContent())
                .province(dto.getProvince())
                .city(dto.getCity())
                .tags(dto.getTags())
                .year(dto.getYear())
                .source(dto.getSource())
                .sourceUrl(dto.getSourceUrl())
                .publishDate(dto.getPublishDate() != null ? dto.getPublishDate() : now)
                .publishUnit(dto.getPublishUnit())
                .regStartDate(dto.getRegStartDate())
                .regEndDate(dto.getRegEndDate())
                .examTime(dto.getExamTime())
                .recruitmentCount(dto.getRecruitmentCount())
                .isTop(dto.getIsTop())
                .isImportant(dto.getIsImportant())
                .sortOrder(dto.getSortOrder())
                .viewCount(0)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        noticeMapper.insert(entity);
        log.info("新增公告成功: id={}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, NoticeUpdateDTO dto) {
        Notice notice = noticeMapper.selectNoticeById(id);
        if (notice == null) {
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
        noticeMapper.updateNotice(notice);
        log.info("更新公告成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 物理删除不受逻辑删除过滤，直接删除并检查影响行数（禁用记录也应可删除）
        int deleted = noticeMapper.physicalDeleteById(id);
        if (deleted == 0) {
            throw new BusinessException(404, "公告不存在");
        }
        log.info("物理删除公告成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        // 用自定义 @Update 直写 is_deleted：MP 的 update(wrapper)/updateById 都会自动注入
        // WHERE is_deleted=false，导致已禁用记录（is_deleted=true）无法更新（0 行）误报 404
        int updated = noticeMapper.updateIsDeleted(id, status == 0);
        if (updated == 0) {
            throw new BusinessException(404, "公告不存在");
        }
        log.info("更新公告状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int deleted = noticeMapper.physicalDeleteBatchIds(ids);
        log.info("批量物理删除公告成功: requested={}, actual={}", ids.size(), deleted);
    }

}
