package com.haifeng.admin.dto.home;

import com.haifeng.common.entity.home.InstitutionClassType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增培训机构
 */
@Data
public class InstitutionAddDTO {

    @NotBlank(message = "机构分类不能为空")
    @Size(max = 32, message = "机构分类最长32字符")
    private String category;

    @NotNull(message = "管理账号不能为空")
    private Long userId;

    @NotBlank(message = "机构名称不能为空")
    @Size(max = 128, message = "机构名称最长128字符")
    private String name;

    @NotBlank(message = "省份不能为空")
    @Size(max = 30, message = "省份最长30字符")
    private String province;

    @Size(max = 32, message = "创办时间最长32字符")
    private String foundedYear;

    @Size(max = 10, message = "轮播图最多10张")
    private List<@NotBlank(message = "轮播图地址不能为空")
            @Size(max = 512, message = "轮播图地址最长512字符")
            @Pattern(regexp = "^(https?://|/).*$", message = "轮播图必须是 http(s) 地址或以/开头的相对路径")
            String> carouselImages;

    @Size(max = 10, message = "标签最多10个")
    private List<@NotBlank(message = "标签不能为空")
            @Size(max = 30, message = "单个标签最长30字符") String> tags;

    @Size(max = 5000, message = "机构介绍最长5000字符")
    private String introduction;

    @Size(max = 64, message = "主打方向最长64字符")
    private String mainDirection;

    @Size(max = 32, message = "班级规模最长32字符")
    private String classSize;

    @Size(max = 256, message = "师资配置最长256字符")
    private String teacherConfig;

    @Size(max = 64, message = "上课形式最长64字符")
    private String courseForm;

    @Valid
    @Size(max = 10, message = "主打班型最多10条")
    private List<InstitutionClassType> classTypes;

    @Size(max = 512, message = "说明最长512字符")
    private String notice;

    @Size(max = 10, message = "机构优势最多10条")
    private List<@NotBlank(message = "机构优势内容不能为空")
            @Size(max = 100, message = "单条机构优势最长100字符") String> advantages;

    @Size(max = 256, message = "地址最长256字符")
    private String address;

    @Size(max = 512, message = "微信二维码地址最长512字符")
    @Pattern(regexp = "^$|^(https?://|/).*$", message = "微信二维码必须是 http(s) 地址或以/开头的相对路径")
    private String wechatQrcodeUrl;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱最长128字符")
    private String email;

    @Size(max = 20, message = "联系电话最长20字符")
    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{5,20}$", message = "联系电话格式不正确")
    private String phone;

    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 999999, message = "排序值最大999999")
    private Integer sortOrder;
}
