package com.haifeng.common.service.algorithm.matcher;

import com.haifeng.common.entity.algorithm.MemberGaokao;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 字段值提取器
 * 从 MemberGaokao 中根据字段名提取对应的值
 */
@Component
public class FieldValueExtractor {

    private static final Map<String, Function<MemberGaokao, Object>> FIELD_GETTERS = new HashMap<>();

    static {
        // 身体视觉条件
        FIELD_GETTERS.put("is_color_blind", MemberGaokao::getIsColorBlind);
        FIELD_GETTERS.put("is_color_weak", MemberGaokao::getIsColorWeak);
        FIELD_GETTERS.put("vision_left", MemberGaokao::getVisionLeft);
        FIELD_GETTERS.put("vision_right", MemberGaokao::getVisionRight);
        FIELD_GETTERS.put("has_smell_disorder", MemberGaokao::getHasSmellDisorder);

        // 身体指标
        FIELD_GETTERS.put("height_cm", MemberGaokao::getHeightCm);
        FIELD_GETTERS.put("weight_kg", MemberGaokao::getWeightKg);
        FIELD_GETTERS.put("is_left_handed", MemberGaokao::getIsLeftHanded);
        FIELD_GETTERS.put("has_tattoo", MemberGaokao::getHasTattoo);
        FIELD_GETTERS.put("has_scar", MemberGaokao::getHasScar);
        FIELD_GETTERS.put("has_stutter", MemberGaokao::getHasStutter);

        // 身份条件
        FIELD_GETTERS.put("is_fresh_graduate", MemberGaokao::getIsFreshGraduate);
        FIELD_GETTERS.put("political_status", MemberGaokao::getPoliticalStatus);
        FIELD_GETTERS.put("household_type", MemberGaokao::getHouseholdType);
        FIELD_GETTERS.put("is_poverty_county", MemberGaokao::getIsPovertyCounty);

        // 外语
        FIELD_GETTERS.put("foreign_language", MemberGaokao::getForeignLanguage);

        // 选科
        FIELD_GETTERS.put("subject_type", MemberGaokao::getSubjectType);
        FIELD_GETTERS.put("second_subject_type", MemberGaokao::getSecondSubjectType);
        FIELD_GETTERS.put("third_subject_type", MemberGaokao::getThirdSubjectType);
    }

    /**
     * 从档案中提取字段值
     *
     * @param gaokao    用户高考档案
     * @param fieldName 字段名（下划线命名）
     * @return 字段值，字段不存在返回 null
     */
    public Object extract(MemberGaokao gaokao, String fieldName) {
        if (gaokao == null || fieldName == null) {
            return null;
        }
        Function<MemberGaokao, Object> getter = FIELD_GETTERS.get(fieldName);
        if (getter == null) {
            return null;
        }
        return getter.apply(gaokao);
    }
}
