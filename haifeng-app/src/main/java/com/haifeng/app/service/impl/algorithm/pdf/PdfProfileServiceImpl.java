package com.haifeng.app.service.impl.algorithm.pdf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.haifeng.app.dto.algorithm.pdf.PdfProfileDTO;
import com.haifeng.app.service.algorithm.pdf.PdfProfileService;
import com.haifeng.app.vo.algorithm.pdf.PdfProfileVO;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * PDF 分析档案服务实现。
 * 说明：MyBatis-Plus 默认 update 策略为 NOT_NULL（null 字段跳过），而本档案需要
 * 支持「用户清空某字段后写回 null」，因此更新时使用 LambdaUpdateWrapper 显式 set 全部字段。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfProfileServiceImpl implements PdfProfileService {

    private final MemberGaokaoMapper memberGaokaoMapper;

    @Override
    public PdfProfileVO getProfile(Long memberId) {
        MemberGaokao archive = selectByMemberId(memberId);
        if (archive == null) {
            return new PdfProfileVO();
        }
        return toVO(archive);
    }

    @Override
    @Transactional
    public void saveProfile(Long memberId, PdfProfileDTO dto) {
        MemberGaokao archive = selectByMemberId(memberId);
        OffsetDateTime now = OffsetDateTime.now();
        if (archive == null) {
            // 基础档案尚未创建（理论上导出 xlsx 后必存在），兜底新建一行，仅写 PDF 档案字段
            MemberGaokao entity = MemberGaokao.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .memberId(memberId)
                    .build();
            applyDto(entity, dto);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            memberGaokaoMapper.insert(entity);
            log.info("创建 PDF 档案成功: memberId={}, archiveId={}", memberId, entity.getId());
        } else {
            LambdaUpdateWrapper<MemberGaokao> wrapper = new LambdaUpdateWrapper<MemberGaokao>()
                    .eq(MemberGaokao::getId, archive.getId())
                    .set(MemberGaokao::getCareerDevPath, dto.getCareerDevPath())
                    .set(MemberGaokao::getPersonalityTraits, dto.getPersonalityTraits())
                    .set(MemberGaokao::getInterestDirection, dto.getInterestDirection())
                    .set(MemberGaokao::getOtherHealthConditions, dto.getOtherHealthConditions())
                    .set(MemberGaokao::getPoliticalReviewStatus, dto.getPoliticalReviewStatus())
                    .set(MemberGaokao::getStayInProvince, dto.getStayInProvince())
                    .set(MemberGaokao::getFamilyResources, dto.getFamilyResources())
                    .set(MemberGaokao::getTuitionAffordability, dto.getTuitionAffordability())
                    .set(MemberGaokao::getAcceptGrassroot, dto.getAcceptGrassroot())
                    .set(MemberGaokao::getAcceptShiftWork, dto.getAcceptShiftWork())
                    .set(MemberGaokao::getAcceptNightWork, dto.getAcceptNightWork())
                    .set(MemberGaokao::getAcceptBusinessTrip, dto.getAcceptBusinessTrip())
                    .set(MemberGaokao::getAcceptRelocation, dto.getAcceptRelocation())
                    .set(MemberGaokao::getRejectedIndustries, dto.getRejectedIndustries())
                    .set(MemberGaokao::getRejectedDirections, dto.getRejectedDirections())
                    .set(MemberGaokao::getUpdatedAt, now);
            memberGaokaoMapper.update(null, wrapper);
            log.info("更新 PDF 档案成功: memberId={}, archiveId={}", memberId, archive.getId());
        }
    }

    private MemberGaokao selectByMemberId(Long memberId) {
        return memberGaokaoMapper.selectOne(
                new LambdaQueryWrapper<MemberGaokao>()
                        .eq(MemberGaokao::getMemberId, memberId)
                        .last("LIMIT 1"));
    }

    private void applyDto(MemberGaokao entity, PdfProfileDTO dto) {
        entity.setCareerDevPath(dto.getCareerDevPath());
        entity.setPersonalityTraits(dto.getPersonalityTraits());
        entity.setInterestDirection(dto.getInterestDirection());
        entity.setOtherHealthConditions(dto.getOtherHealthConditions());
        entity.setPoliticalReviewStatus(dto.getPoliticalReviewStatus());
        entity.setStayInProvince(dto.getStayInProvince());
        entity.setFamilyResources(dto.getFamilyResources());
        entity.setTuitionAffordability(dto.getTuitionAffordability());
        entity.setAcceptGrassroot(dto.getAcceptGrassroot());
        entity.setAcceptShiftWork(dto.getAcceptShiftWork());
        entity.setAcceptNightWork(dto.getAcceptNightWork());
        entity.setAcceptBusinessTrip(dto.getAcceptBusinessTrip());
        entity.setAcceptRelocation(dto.getAcceptRelocation());
        entity.setRejectedIndustries(dto.getRejectedIndustries());
        entity.setRejectedDirections(dto.getRejectedDirections());
    }

    private PdfProfileVO toVO(MemberGaokao entity) {
        PdfProfileVO vo = new PdfProfileVO();
        vo.setCareerDevPath(entity.getCareerDevPath());
        vo.setPersonalityTraits(entity.getPersonalityTraits());
        vo.setInterestDirection(entity.getInterestDirection());
        vo.setOtherHealthConditions(entity.getOtherHealthConditions());
        vo.setPoliticalReviewStatus(entity.getPoliticalReviewStatus());
        vo.setStayInProvince(entity.getStayInProvince());
        vo.setFamilyResources(entity.getFamilyResources());
        vo.setTuitionAffordability(entity.getTuitionAffordability());
        vo.setAcceptGrassroot(entity.getAcceptGrassroot());
        vo.setAcceptShiftWork(entity.getAcceptShiftWork());
        vo.setAcceptNightWork(entity.getAcceptNightWork());
        vo.setAcceptBusinessTrip(entity.getAcceptBusinessTrip());
        vo.setAcceptRelocation(entity.getAcceptRelocation());
        vo.setRejectedIndustries(entity.getRejectedIndustries());
        vo.setRejectedDirections(entity.getRejectedDirections());
        return vo;
    }
}
