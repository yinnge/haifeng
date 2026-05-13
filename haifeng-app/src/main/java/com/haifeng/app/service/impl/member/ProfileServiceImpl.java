package com.haifeng.app.service.impl.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.service.member.ProfileService;
import com.haifeng.app.vo.member.ProfileVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.user.MemberProfile;
import com.haifeng.common.enums.GenderEnum;
import com.haifeng.common.enums.IdentityEnum;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.mapper.user.MemberProfileMapper;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final MemberProfileMapper profileMapper;
    private final UniversityMapper universityMapper;
    private final CityMapper cityMapper;
    private final MajorMapper majorMapper;

    @Override
    public ProfileVO getProfile() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>()
                        .eq(MemberProfile::getMemberId, memberId));

        // 若不存在则自动创建空记录
        if (profile == null) {
            profile = createEmptyProfile(memberId);
        }

        boolean canEditSchool = IdentityEnum.canHaveSchool(profile.getIdentity());

        return ProfileVO.builder()
                .realName(profile.getRealName())
                .email(profile.getEmail())
                .gender(profile.getGender())
                .schoolName(canEditSchool ? profile.getSchoolName() : null)
                .province(profile.getProvince())
                .city(profile.getCity())
                .major(profile.getMajor())
                .identity(profile.getIdentity())
                .grade(profile.getGrade())
                .educationLevel(profile.getEducationLevel())
                .favoriteCount(profile.getFavoriteCount())
                .viewCount(profile.getViewCount())
                .canEditSchool(canEditSchool)
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(ProfileUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>()
                        .eq(MemberProfile::getMemberId, memberId));

        if (profile == null) {
            profile = createEmptyProfile(memberId);
        }

        // 校验枚举值
        if (dto.getGender() != null && !GenderEnum.isValid(dto.getGender())) {
            throw new BusinessException(400, "性别值无效");
        }
        if (dto.getIdentity() != null && !IdentityEnum.isValid(dto.getIdentity())) {
            throw new BusinessException(400, "身份值无效");
        }
        if (dto.getProvince() != null && !ProvinceEnum.isValid(dto.getProvince())) {
            throw new BusinessException(400, "省份值无效");
        }

        // 校验关联数据存在性
        if (StringUtils.hasText(dto.getCity())) {
            Long count = cityMapper.selectCount(
                    new LambdaQueryWrapper<City>().eq(City::getCityName, dto.getCity()));
            if (count == 0) {
                throw new BusinessException(400, "城市不存在");
            }
        }
        if (StringUtils.hasText(dto.getMajor())) {
            Long count = majorMapper.selectCount(
                    new LambdaQueryWrapper<Major>().eq(Major::getMajorName, dto.getMajor()));
            if (count == 0) {
                throw new BusinessException(400, "专业不存在");
            }
        }

        // 确定最终的identity值
        String finalIdentity = dto.getIdentity() != null ? dto.getIdentity() : profile.getIdentity();
        boolean canEditSchool = IdentityEnum.canHaveSchool(finalIdentity);

        // 校验学校（仅大学生/研究生可填）
        if (StringUtils.hasText(dto.getSchoolName())) {
            if (!canEditSchool) {
                throw new BusinessException(400, "当前身份不支持填写学校");
            }
            Long count = universityMapper.selectCount(
                    new LambdaQueryWrapper<University>().eq(University::getName, dto.getSchoolName()));
            if (count == 0) {
                throw new BusinessException(400, "学校不存在");
            }
        }

        // 更新字段（非null才更新）
        if (dto.getRealName() != null) profile.setRealName(dto.getRealName());
        if (dto.getEmail() != null) profile.setEmail(dto.getEmail());
        if (dto.getGender() != null) profile.setGender(dto.getGender());
        if (dto.getProvince() != null) profile.setProvince(dto.getProvince());
        if (dto.getCity() != null) profile.setCity(dto.getCity());
        if (dto.getMajor() != null) profile.setMajor(dto.getMajor());
        if (dto.getIdentity() != null) profile.setIdentity(dto.getIdentity());
        if (dto.getGrade() != null) profile.setGrade(dto.getGrade());
        if (dto.getEducationLevel() != null) profile.setEducationLevel(dto.getEducationLevel());

        // 学校处理
        if (canEditSchool && dto.getSchoolName() != null) {
            profile.setSchoolName(dto.getSchoolName());
        } else if (!canEditSchool) {
            // 身份改为高中生/其他时，清空学校
            profile.setSchoolName(null);
        }

        profile.setUpdatedAt(OffsetDateTime.now());
        profileMapper.updateById(profile);

        log.info("更新用户资料成功: memberId={}", memberId);
    }

    private MemberProfile createEmptyProfile(Long memberId) {
        MemberProfile profile = MemberProfile.builder()
                .id(SnowflakeIdGenerator.nextId())
                .memberId(memberId)
                .favoriteCount(0)
                .viewCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        profileMapper.insert(profile);
        return profile;
    }
}
