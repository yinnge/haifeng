package com.haifeng.admin.service.impl.employment.contentManagement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.notice.NoticeUpdateDTO;
import com.haifeng.admin.excel.employment.contentManagement.NoticeExcelDTO;
import com.haifeng.admin.service.employment.contentManagement.NoticeService;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.notice.NoticeListVO;
import com.haifeng.common.entity.employment.contentManagement.Notice;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.NoticeMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    public void update(Long id, NoticeUpdateDTO dto) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }
        BeanUtils.copyProperties(dto, notice);
        notice.setUpdatedAt(OffsetDateTime.now());
        noticeMapper.updateById(notice);
        log.info("更新公告成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        noticeMapper.deleteById(id);
        log.info("硬删除公告成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        notice.setIsDeleted(status == 0);
        notice.setUpdatedAt(OffsetDateTime.now());
        noticeMapper.updateById(notice);
        log.info("更新公告状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            Notice notice = noticeMapper.selectById(id);
            if (notice != null) {
                noticeMapper.deleteById(id);
            }
        }
        log.info("批量删除公告成功: count={}", ids.size());
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<NoticeExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (NoticeExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getNoticeCategory())) {
                errors.add("公告类别不能为空");
            }
            if (!StringUtils.hasText(dto.getTitle())) {
                errors.add("标题不能为空");
            }
            if (StringUtils.hasText(dto.getProvince()) && !ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ");
                errorMsg.append(String.join("; ", errors));
                errorMsg.append("\n");
            }
        }
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<NoticeExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (NoticeExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getNoticeCategory())) errors.add("公告类别不能为空");
            if (!StringUtils.hasText(dto.getTitle())) errors.add("标题不能为空");
            if (StringUtils.hasText(dto.getProvince()) && !ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (NoticeExcelDTO dto : list) {
            Notice entity = Notice.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .noticeCategory(dto.getNoticeCategory())
                    .noticeType(dto.getNoticeType())
                    .title(dto.getTitle())
                    .summary(dto.getSummary())
                    .content(dto.getContent())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .tags(StringUtils.hasText(dto.getTags()) ? dto.getTags().split(",") : new String[0])
                    .year(dto.getYear())
                    .source(dto.getSource())
                    .sourceUrl(dto.getSourceUrl())
                    .publishDate(dto.getPublishDate())
                    .publishUnit(dto.getPublishUnit())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .examTime(dto.getExamTime())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .isTop(dto.getIsTop() != null && dto.getIsTop())
                    .isImportant(dto.getIsImportant() != null && dto.getIsImportant())
                    .sortOrder(dto.getSortOrder())
                    .viewCount(0)
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            noticeMapper.insert(entity);
        }
        log.info("导入公告成功: count={}", list.size());
    }

    private List<NoticeExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(NoticeExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
