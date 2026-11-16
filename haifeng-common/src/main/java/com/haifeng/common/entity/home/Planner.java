package com.haifeng.common.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.config.JsonbTypeHandler;
import com.haifeng.common.config.StringListTypeHandler;
import com.haifeng.common.dto.home.PlannerServiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 规划师。
 *
 * <p>约定（2026-11-16 改造）：
 * <ul>
 *   <li>无 {@code status} 上下架字段：上下架语义由 {@code deleted} 软删除承担</li>
 *   <li>禁用 {@code @TableLogic}：所有查询/删除由各端 service 手动写 {@code is_deleted = false} 条件</li>
 *   <li>{@code version} 仅保留列，不加 {@code @Version}（不启用乐观锁）</li>
 *   <li>{@code userId} 逻辑关联 t_member.id，不建物理外键</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_planners", autoResultMap = true)
public class Planner {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联用户 ID（逻辑外键，指向 t_member.id） */
    private Long userId;

    /** 规划师姓名 */
    private String name;

    /** 服务对象：高考 / 考研（ServiceTargetEnum 校验，DB 不加 CHECK） */
    private String serviceTarget;

    /** 职位，例：首席 */
    private String position;

    /** 所属省份（ProvinceEnum 裸值，如 北京 / 广西） */
    private String province;

    /** 列表描述 */
    private String listDesc;

    /** 列表 tag：{陕西新高考, 位次精算} */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> listTags;

    /** svg 头像（本地静态资源相对路径，可空） */
    private String avatarUrl;

    /** 从业年限，例：11年 */
    private String workYears;

    /** 服务考生，例：3200+ */
    private String servedCount;

    /** 初稿时效，例：48h */
    private String draftSla;

    /** 认证描述，例：持证生涯规划师 */
    private String certDesc;

    /** 详情大段介绍 */
    private String description;

    /** 详情 tag：{陕西新高考, 专业组梯度} */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> detailTags;

    /** 服务内容：[{"title":"首次诊断","content":"..."}] */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<PlannerServiceItem> services;

    /** 邮箱（可空） */
    private String email;

    /** 手机号（可空，不唯一） */
    private String phone;

    /** 地址（可空） */
    private String address;

    /** 微信二维码 URL（本地静态资源相对路径，可空） */
    private String wechatQrcodeUrl;

    /** 软删除标记（手动维护，无 @TableLogic） */
    @TableField("is_deleted")
    private Boolean deleted;

    /** 展示排序，越小越靠前 */
    private Integer sortOrder;

    /** 乐观锁版本号列（保留，未启用 @Version 拦截器） */
    private Integer version;

    /** 创建人（后台用户 ID） */
    private Long createdBy;

    /** 更新人（后台用户 ID） */
    private Long updatedBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
