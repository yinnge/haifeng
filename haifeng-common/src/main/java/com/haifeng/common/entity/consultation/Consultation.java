package com.haifeng.common.entity.consultation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haifeng.common.enums.ConsultationBizType;
import com.haifeng.common.enums.ConsultationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 咨询留言：C 端用户向规划师 / 培训机构提交的预约留言。
 *
 * <p>约定：
 * <ul>
 *   <li>{@code teacherMemberId} 是接收方教师账号（逻辑关联 t_member.id），不是提交人</li>
 *   <li>{@code bizName} 是提交时的业务名称快照</li>
 *   <li>无 {@code @TableLogic}，不做软删除</li>
 *   <li>{@code version} 仅保留列，不启用 {@code @Version}；标记已处理用条件更新保证幂等</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_consultation")
public class Consultation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 业务类型：planner / institution */
    private ConsultationBizType bizType;

    /** 业务ID：t_planners.id / t_institutions.id */
    private Long bizId;

    /** 提交时的业务名称快照 */
    private String bizName;

    /** 接收方教师账号（t_member.id） */
    private Long teacherMemberId;

    /** 提交人会员ID，未登录为空 */
    private Long senderMemberId;

    /** 留言 */
    private String message;

    /** 微信号（可选） */
    private String wechat;

    /** 联系电话（必填） */
    private String phone;

    /** 提交IP */
    private String clientIp;

    /** 处理状态 */
    private ConsultationStatus status;

    /** 保留列，不启用乐观锁 */
    private Integer version;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
