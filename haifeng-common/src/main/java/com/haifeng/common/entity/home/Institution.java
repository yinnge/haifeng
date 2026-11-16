package com.haifeng.common.entity.home;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.haifeng.common.config.InstitutionClassTypeListTypeHandler;
import com.haifeng.common.config.StringListTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 培训机构（首页管理）
 * <p>category 只允许 4 个值，见 {@link com.haifeng.common.enums.InstitutionCategoryEnum}；
 * user_id 逻辑关联 t_member（必须是 is_teacher = true），不建物理外键。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_institutions", autoResultMap = true)
public class Institution {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分类：高中冲刺小班/考公考编机构/央国企培训/职业培训机构
     */
    private String category;

    /**
     * 管理账号ID（t_member.id，必须为老师），全局唯一
     */
    private Long userId;

    private String name;

    /**
     * 省份（中文，与 ProvinceEnum.desc 一致）
     */
    private String province;

    /**
     * 创办时间，如 2013年
     */
    private String foundedYear;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> carouselImages;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> tags;

    private String introduction;

    /**
     * 主打方向，如 美术/音乐/传媒文化课
     */
    private String mainDirection;

    /**
     * 班级规模，如 15人以内
     */
    private String classSize;

    /**
     * 师资配置
     */
    private String teacherConfig;

    /**
     * 上课形式，如 联考后全日制
     */
    private String courseForm;

    /**
     * 主打班型：[{title, introduction, tag}]
     */
    @TableField(typeHandler = InstitutionClassTypeListTypeHandler.class)
    private List<InstitutionClassType> classTypes;

    /**
     * 说明/温馨提示
     */
    private String notice;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> advantages;

    private String address;

    /**
     * 微信二维码（服务器相对路径）
     */
    private String wechatQrcodeUrl;

    private String email;

    private String phone;

    /**
     * 0-下架 1-上架
     */
    private Short status;

    /**
     * 排序，越小越靠前
     */
    private Integer sortOrder;

    @Version
    private Integer version;

    /**
     * 创建人 sys_admin.id
     */
    private Long createdBy;

    /**
     * 更新人 sys_admin.id
     */
    private Long updatedBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
