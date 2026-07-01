package com.haifeng.app.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.special.StrongBaseUniversityService;
import com.haifeng.app.vo.special.StrongBaseUniversityDetailVO;
import com.haifeng.common.entity.special.StrongBaseUniversity;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseUniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseUniversityServiceImpl implements StrongBaseUniversityService {

    private final StrongBaseUniversityMapper strongBaseUniversityMapper;

    @Override
    public StrongBaseUniversityDetailVO detailByUniversityId(Long universityId) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectOne(
                new LambdaQueryWrapper<StrongBaseUniversity>()
                        .eq(StrongBaseUniversity::getUniversityId, universityId));
        if (entity == null) {
            log.debug("强基院校配置不存在, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        return StrongBaseUniversityDetailVO.builder()
                .id(entity.getId())
                .universityId(entity.getUniversityId())
                .universityName(entity.getUniversityName())
                .isPilot(entity.getIsPilot())
                .pilotYear(entity.getPilotYear())
                .officialUrl(entity.getOfficialUrl())
                .signupUrl(entity.getSignupUrl())
                .testBeforeScore(entity.getTestBeforeScore())
                .defaultEntryRatio(entity.getDefaultEntryRatio())
                .defaultAdmissionFormula(entity.getDefaultAdmissionFormula())
                .availableMajors(entity.getAvailableMajors())
                .specialNotes(entity.getSpecialNotes())
                .build();
    }
}
