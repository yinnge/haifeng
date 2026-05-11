package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.AnnouncementAddDTO;
import com.haifeng.admin.dto.home.AnnouncementQueryDTO;
import com.haifeng.admin.dto.home.AnnouncementUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.AnnouncementService;
import com.haifeng.admin.vo.home.AnnouncementDetailVO;
import com.haifeng.admin.vo.home.AnnouncementListVO;
import com.haifeng.common.entity.home.Announcement;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.AnnouncementMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public IPage<AnnouncementListVO> page(AnnouncementQueryDTO dto) {
        Page<Announcement> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getDeleted, false);

        // 标题模糊查询
        if (StringUtils.hasText(dto.getTitle())) {
            wrapper.like(Announcement::getTitle, dto.getTitle());
        }
        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(Announcement::getStatus, dto.getStatus());
        }

        // 按更新时间降序
        wrapper.orderByDesc(Announcement::getUpdatedAt);

        IPage<Announcement> announcementPage = announcementMapper.selectPage(page, wrapper);

        return announcementPage.convert(announcement -> {
            AnnouncementListVO vo = new AnnouncementListVO();
            BeanUtils.copyProperties(announcement, vo);
            return vo;
        });
    }

    @Override
    public AnnouncementDetailVO detail(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }

        AnnouncementDetailVO vo = new AnnouncementDetailVO();
        BeanUtils.copyProperties(announcement, vo);
        return vo;
    }

    @Override
    public Long add(AnnouncementAddDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        Announcement announcement = Announcement.builder()
                .id(id)
                .title(dto.getTitle())
                .content(dto.getContent())
                .tag(dto.getTag())
                .status((short) 1)  // 默认展示状态
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        announcementMapper.insert(announcement);

        log.info("新增公告成功: id={}, title={}", id, dto.getTitle());
        return id;
    }

    @Override
    public void update(Long id, AnnouncementUpdateDTO dto) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }

        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setTag(dto.getTag());
        announcement.setUpdatedAt(OffsetDateTime.now());

        announcementMapper.updateById(announcement);

        log.info("更新公告成功: id={}, title={}", id, dto.getTitle());
    }

    @Override
    public void updateStatus(Long id, StatusDTO dto) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }

        announcement.setStatus(dto.getStatus());
        announcement.setUpdatedAt(OffsetDateTime.now());

        announcementMapper.updateById(announcement);

        log.info("更新公告状态成功: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public void delete(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }

        announcementMapper.deleteById(id);

        log.info("硬删除公告成功: id={}", id);
    }
}
