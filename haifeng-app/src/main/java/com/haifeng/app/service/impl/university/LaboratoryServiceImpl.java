package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.LaboratoryQueryDTO;
import com.haifeng.app.service.university.LaboratoryService;
import com.haifeng.app.vo.university.LaboratoryDetailVO;
import com.haifeng.app.vo.university.LaboratoryListVO;
import com.haifeng.common.entity.university.Laboratory;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.LaboratoryMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl implements LaboratoryService {

    private static final short STATUS_PUBLISHED = 1;

    private final LaboratoryMapper laboratoryMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<LaboratoryListVO> page(Long universityId, LaboratoryQueryDTO dto) {
        University univ = universityMapper.selectById(universityId);
        if (univ == null || univ.getStatus() == null || univ.getStatus() != STATUS_PUBLISHED) {
            log.debug("院校不存在或已下架, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        Page<Laboratory> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Laboratory> wrapper = new LambdaQueryWrapper<Laboratory>()
                .eq(Laboratory::getUniversityId, universityId)
                .eq(Laboratory::getStatus, STATUS_PUBLISHED)
                .orderByAsc(Laboratory::getSortOrder)
                .orderByDesc(Laboratory::getId);

        IPage<Laboratory> entityPage = laboratoryMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public LaboratoryDetailVO detail(Long labId) {
        Laboratory e = laboratoryMapper.selectOne(
                new LambdaQueryWrapper<Laboratory>()
                        .eq(Laboratory::getId, labId)
                        .eq(Laboratory::getStatus, STATUS_PUBLISHED));
        if (e == null) {
            log.debug("实验室不存在或已下架, labId={}", labId);
            throw new BusinessException(ResultCode.NOT_FOUND, "实验室不存在");
        }

        return LaboratoryDetailVO.builder()
                .universityName(e.getUniversityName())
                .labType(e.getLabType())
                .establishedYear(e.getEstablishedYear())
                .region(e.getRegion())
                .department(e.getDepartment())
                .director(e.getDirector())
                .staffCount(e.getStaffCount())
                .studentCount(e.getStudentCount())
                .email(desensitizeEmail(e.getEmail()))
                .phone(desensitizePhone(e.getPhone()))
                .introduction(e.getIntroduction())
                .researchDescription(e.getResearchDescription())
                .labSpace(e.getLabSpace())
                .openTopics(e.getOpenTopics())
                .cooperation(e.getCooperation())
                .visitingScholars(e.getVisitingScholars())
                .researchFields(e.getResearchFields())
                .statistics(e.getStatistics())
                .majorEquipment(e.getMajorEquipment())
                .coreTeam(e.getCoreTeam())
                .build();
    }

    private LaboratoryListVO toListVO(Laboratory e) {
        return LaboratoryListVO.builder()
                .id(e.getId())
                .name(e.getName())
                .labType(e.getLabType())
                .build();
    }

    private String desensitizePhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String desensitizeEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        String prefix = email.substring(0, atIndex);
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***@" + email.substring(atIndex + 1);
        }
        return prefix.substring(0, 2) + "***@" + email.substring(atIndex + 1);
    }
}
