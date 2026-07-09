package com.haifeng.admin.vo.auth;

import com.haifeng.common.vo.auth.TokenVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTokenVO extends TokenVO {

    /**
     * 管理员拥有的模块编码列表（前端据此渲染菜单）
     */
    private List<String> permissions;
}
