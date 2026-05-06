package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.MemberQueryDTO;
import com.haifeng.admin.dto.user.MemberStatusDTO;
import com.haifeng.admin.service.user.MemberService;
import com.haifeng.admin.vo.user.MemberDetailVO;
import com.haifeng.admin.vo.user.MemberListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public R<IPage<MemberListVO>> list(@Valid MemberQueryDTO dto) {
        return R.ok(memberService.page(dto));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public R<MemberDetailVO> detail(@PathVariable Long id) {
        return R.ok(memberService.detail(id));
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "用户管理", action = "修改用户状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody MemberStatusDTO dto) {
        memberService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 查看用户微信明文（强制记录操作日志）
     */
    @GetMapping("/{id}/wechat")
    @OperationLog(module = "用户管理", action = "查看用户微信明文")
    public R<String> getWechat(@PathVariable Long id) {
        return R.ok(memberService.getWechatPlaintext(id));
    }
}
