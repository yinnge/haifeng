package com.haifeng.common.service.algorithm.matcher;

import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SubjectMatcher {

    public SubjectMatchResult match(MemberGaokao gaokao, AdmissionGroup group) {
        List<String> userSubjects = Arrays.asList(
                gaokao.getSubjectType(),
                gaokao.getSecondSubjectType(),
                gaokao.getThirdSubjectType()
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());

        List<String> groupSubjects = group.getSubjects();
        String reqType = group.getRequirementType();

        // 不限 → 永远符合
        if ("不限".equals(reqType) || groupSubjects == null || groupSubjects.isEmpty()) {
            return SubjectMatchResult.ok();
        }

        // 计算交集数量
        long matchCount = groupSubjects.stream()
                .filter(userSubjects::contains)
                .count();

        switch (reqType) {
            case "2选1":
            case "3选1":
                if (matchCount >= 1) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("需从 " + String.join("/", groupSubjects) + " 中选考至少1门");

            case "必选1":
                if (matchCount >= 1) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须选考 " + groupSubjects.get(0));

            case "必选2":
                if (matchCount >= 2) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须同时选考 " + String.join(" 和 ", groupSubjects));

            case "必选3":
                if (matchCount >= 3) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须同时选考 " + String.join("、", groupSubjects));

            default:
                return SubjectMatchResult.ok();
        }
    }
}
