package com.haifeng.app.service.impl.algorithm.admission;

import com.haifeng.app.dto.algorithm.admission.GuestArchiveQueryDTO;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.service.algorithm.ProvinceReformService;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 专业组/专业查询用的「高考档案」解析器。
 *
 * <p>解析顺序：</p>
 * <ol>
 *   <li>已登录且库中存在档案 → 用服务端档案（权威）；</li>
 *   <li>否则用请求携带的游客档案参数组装<b>临时</b> {@link MemberGaokao}（不落库、不写 Redis）；</li>
 *   <li>两者都没有 → 返回 {@code null}，由调用方抛 {@code GAOKAO_ARCHIVE_NOT_FOUND}。</li>
 * </ol>
 *
 * <p>这样未注册用户填写档案（仅存浏览器）后也能正常查询专业组，
 * 而不必为"模拟填报"在服务端建号建档。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestArchiveResolver {

    private final MemberGaokaoMapper memberGaokaoMapper;
    private final ProvinceReformService provinceReformService;

    /**
     * @return 可用于算法计算的高考档案；无服务端档案且请求未携带完整游客档案时返回 null
     */
    public MemberGaokao resolve(GuestArchiveQueryDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (memberId != null) {
            MemberGaokao stored = memberGaokaoMapper.selectByMemberId(memberId);
            if (stored != null) {
                return stored;
            }
            log.debug("登录用户暂无服务端档案，尝试使用请求携带的游客档案: memberId={}", memberId);
        }
        return buildFromRequest(dto);
    }

    /**
     * 用请求参数组装临时档案。省份 / 年份 / 分数是安全系数计算的必需项，
     * 缺任一项视为「未建档」返回 null（前端会引导去填写档案）。
     */
    private MemberGaokao buildFromRequest(GuestArchiveQueryDTO dto) {
        if (dto == null
                || !StringUtils.hasText(dto.getGaokaoProvince())
                || dto.getGaokaoYear() == null
                || dto.getScore() == null) {
            return null;
        }

        MemberGaokao guest = MemberGaokao.builder()
                .gaokaoYear(dto.getGaokaoYear())
                .gaokaoProvince(dto.getGaokaoProvince())
                .score(dto.getScore())
                .rank(dto.getRank())
                .subjectType(dto.getSubjectType())
                .secondSubjectType(dto.getSecondSubjectType())
                .thirdSubjectType(dto.getThirdSubjectType())
                .batchLineScore(dto.getBatchLineScore())
                .build();

        // 线上分差：与档案入库时的计算口径保持一致（总分 - 批次省控线）
        if (dto.getBatchLineScore() != null) {
            guest.setScoreAboveLine(dto.getScore() - dto.getBatchLineScore());
        }

        // 改革模式不入参：由省份 + 年份在服务端推导，避免前端与 DB 口径不一致
        try {
            guest.setReformModel(
                    provinceReformService.getReformModel(dto.getGaokaoProvince(), dto.getGaokaoYear()));
        } catch (Exception e) {
            log.warn("游客档案改革模式推导失败: province={}, year={}",
                    dto.getGaokaoProvince(), dto.getGaokaoYear(), e);
        }
        return guest;
    }
}
