package com.haifeng.common.service.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.common.entity.algorithm.ProvinceReform;
import com.haifeng.common.mapper.algorithm.ProvinceReformMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvinceReformService {

    private final ProvinceReformMapper provinceReformMapper;

    /**
     * 判断某省某年是否已实行新高考
     * 区间逻辑：倒序遍历该省所有改革记录，找到第一个 reformYear <= gaokaoYear 的
     * 若找到 → 新高考（不管是 3+3 还是 3+1+2）
     * 若找不到（用户年份在所有改革之前）→ 旧高考
     */
    public boolean isNewGaokao(String province, Short gaokaoYear) {
        if (province == null || gaokaoYear == null) return false;

        List<ProvinceReform> reforms = provinceReformMapper.selectList(
                new LambdaQueryWrapper<ProvinceReform>()
                        .eq(ProvinceReform::getProvince, province)
                        .orderByAsc(ProvinceReform::getReformYear)
        );

        for (int i = reforms.size() - 1; i >= 0; i--) {
            ProvinceReform reform = reforms.get(i);
            if (reform.getReformYear() != null && gaokaoYear >= reform.getReformYear()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取某省某年适用的改革年份
     * 用于判断历史录取数据属于新高考还是旧高考
     * 返回最接近且小于等于 targetYear 的改革年份
     * 若所有改革年份都大于 targetYear，返回第一个改革年份（最早的那个）
     * 若没有任何改革记录，返回 null
     */
    public Short getEffectiveReformYear(String province, Short targetYear) {
        if (province == null) return null;

        List<ProvinceReform> reforms = provinceReformMapper.selectList(
                new LambdaQueryWrapper<ProvinceReform>()
                        .eq(ProvinceReform::getProvince, province)
                        .orderByAsc(ProvinceReform::getReformYear)
        );

        if (reforms.isEmpty()) return null;

        // 倒序遍历，找到第一个 reformYear <= targetYear
        for (int i = reforms.size() - 1; i >= 0; i--) {
            ProvinceReform reform = reforms.get(i);
            if (reform.getReformYear() != null && targetYear >= reform.getReformYear()) {
                return reform.getReformYear();
            }
        }

        // targetYear 在所有改革之前，返回最早的改革年份作为"改革起点"
        return reforms.get(0).getReformYear();
    }

    /**
     * 获取某省最早的改革年份（用于判断历史录取数据是否属于新高考）
     * 返回所有改革记录中最小的 reformYear
     * 若没有任何改革记录，返回 null
     */
    public Short getEarliestReformYear(String province) {
        if (province == null) return null;
        return provinceReformMapper.selectMinReformYearByProvince(province);
    }

    /**
     * 获取某省某年适用的改革模式
     * 与 GaokaoArchiveServiceImpl 中的 determineReformModel 逻辑一致
     */
    public String getReformModel(String province, Short gaokaoYear) {
        if (province == null || gaokaoYear == null) {
            return "传统文理";
        }

        List<ProvinceReform> reforms = provinceReformMapper.selectList(
                new LambdaQueryWrapper<ProvinceReform>()
                        .eq(ProvinceReform::getProvince, province)
                        .orderByAsc(ProvinceReform::getReformYear)
        );

        if (reforms.isEmpty()) {
            return "传统文理";
        }

        for (int i = reforms.size() - 1; i >= 0; i--) {
            ProvinceReform reform = reforms.get(i);
            if (reform.getReformYear() == null) {
                return "传统文理";
            }
            if (gaokaoYear >= reform.getReformYear()) {
                return reform.getReformModel();
            }
        }

        return "传统文理";
    }
}
