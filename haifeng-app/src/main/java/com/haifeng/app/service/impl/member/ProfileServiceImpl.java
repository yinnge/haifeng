package com.haifeng.app.service.impl.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.service.member.ProfileService;
import com.haifeng.app.vo.member.ProfileVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberFavorite;
import com.haifeng.common.entity.user.MemberProfile;
import com.haifeng.common.enums.GenderEnum;
import com.haifeng.common.enums.IdentityEnum;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.mapper.user.MemberFavoriteMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberProfileMapper;
import com.haifeng.common.service.resource.OssService;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final MemberProfileMapper profileMapper;
    private final MemberFavoriteMapper memberFavoriteMapper;
    private final UniversityMapper universityMapper;
    private final CityMapper cityMapper;
    private final MajorMapper majorMapper;
    private final MemberMapper memberMapper;
    private final OssService ossService;

    /** 头像允许的格式（不含 svg：用户自制头像走公共读 bucket，svg 会引入存储型 XSS） */
    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("png", "jpg", "jpeg");
    /** 头像文件大小上限：2MB */
    private static final long MAX_AVATAR_SIZE = 2L * 1024 * 1024;
    /** 头像像素下限（避免糊图 / 1x1 占位图） */
    private static final int MIN_AVATAR_DIMENSION = 64;
    /** 头像像素上限（C 端展示直径 ≤ 160px，2048 足够且防超大图滥用） */
    private static final int MAX_AVATAR_DIMENSION = 2048;
    /** 宽高比下限（避免极端细长图） */
    private static final double MIN_AVATAR_RATIO = 0.5;
    /** 宽高比上限 */
    private static final double MAX_AVATAR_RATIO = 2.0;

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

        Long favoriteCount = memberFavoriteMapper.selectCount(
                new LambdaQueryWrapper<MemberFavorite>()
                        .eq(MemberFavorite::getMemberId, memberId));

        return ProfileVO.builder()
                .email(profile.getEmail())
                .gender(profile.getGender())
                .schoolName(canEditSchool ? profile.getSchoolName() : null)
                .province(profile.getProvince())
                .city(profile.getCity())
                .major(profile.getMajor())
                .identity(profile.getIdentity())
                .grade(profile.getGrade())
                .educationLevel(profile.getEducationLevel())
                .favoriteCount(favoriteCount == null ? 0 : favoriteCount.intValue())
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
            String cityName = dto.getCity();
            Long count = cityMapper.selectCount(
                    new LambdaQueryWrapper<City>()
                            .like(City::getCityName, cityName)
                            .or()
                            .like(City::getCityName, cityName + "市")
                            .or()
                            .like(City::getCityName, cityName.replace("市", "")));
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

        // 更新 profile 字段（非null才更新）
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

    @Override
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的头像");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(400, "头像大小不能超过 2MB");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "头像仅支持 JPG / PNG 格式");
        }

        byte[] data;
        BufferedImage image;
        try {
            data = file.getBytes();
            image = ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException e) {
            throw new BusinessException(400, "头像读取失败，请重新选择");
        }
        if (image == null) {
            throw new BusinessException(400, "无法识别的图片格式");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        if (width < MIN_AVATAR_DIMENSION || height < MIN_AVATAR_DIMENSION) {
            throw new BusinessException(400, "头像尺寸不能小于 64×64 像素");
        }
        if (width > MAX_AVATAR_DIMENSION || height > MAX_AVATAR_DIMENSION) {
            throw new BusinessException(400, "头像尺寸不能超过 2048×2048 像素");
        }
        double ratio = (double) width / height;
        if (ratio < MIN_AVATAR_RATIO || ratio > MAX_AVATAR_RATIO) {
            throw new BusinessException(400, "头像宽高比例需在 1:2 至 2:1 之间");
        }

        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 固定 objectKey 覆盖上传：换头像即自动「删除旧图、写入新图」，无需手动删对象。
        // 用户切换到内置静态头像时不调用本接口，OSS 上的自定义头像会保留，便于随时切回。
        String url = ossService.uploadMemberAvatar(data, "." + extension, memberId);
        member.setAvatar(url);
        memberMapper.updateById(member);

        log.info("上传会员头像成功: memberId={}, size={}, dimension={}x{}", memberId, data.length, width, height);
        return url;
    }

    /** 从文件名提取小写扩展名（不含点）；无扩展名返回空串 */
    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
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
