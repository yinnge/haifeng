package com.haifeng.app.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 带「游客档案」的分页查询基类。
 *
 * <p>背景：专业组/专业查询接口免登录，但后端计算选科过滤与安全系数依赖高考档案
 * （{@code t_member_gaokao}）。游客的档案只存在浏览器 localStorage（不落库、不注册），
 * 因此这些档案字段以查询参数形式随请求上行，由
 * {@code GuestArchiveResolver} 组装成临时 {@code MemberGaokao} 参与计算。</p>
 *
 * <p>已登录且库中已有档案时，服务端档案优先，这些参数被忽略。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GuestArchiveQueryDTO extends BasePageQueryDTO {

    /** 高考省份（裸省份名，如 北京 / 广西，与 DB 字段对齐） */
    @Size(max = 20, message = "高考省份不能超过20字")
    private String gaokaoProvince;

    /** 高考年份 */
    @Min(value = 2000, message = "高考年份不合法")
    @Max(value = 2100, message = "高考年份不合法")
    private Short gaokaoYear;

    /** 高考总分（上限留到 1000，兼容海南等 900 分制省份） */
    @Min(value = 0, message = "高考总分不能小于0")
    @Max(value = 1000, message = "高考总分不合法")
    private Integer score;

    /** 位次 */
    @Min(value = 0, message = "位次不能小于0")
    private Integer rank;

    /** 首选科目 / 文理类别 */
    @Size(max = 20, message = "科目名称不能超过20字")
    private String subjectType;

    /** 第二科目 */
    @Size(max = 20, message = "科目名称不能超过20字")
    private String secondSubjectType;

    /** 第三科目 */
    @Size(max = 20, message = "科目名称不能超过20字")
    private String thirdSubjectType;

    /** 批次省控线分数（用于计算「线上分差」） */
    @Min(value = 0, message = "省控线不能小于0")
    private Integer batchLineScore;
}
