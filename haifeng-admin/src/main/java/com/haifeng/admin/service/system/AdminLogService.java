package com.haifeng.admin.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.system.AdminLogBatchDeleteDTO;
import com.haifeng.admin.dto.system.AdminLogQueryDTO;
import com.haifeng.admin.vo.system.AdminLogDetailVO;
import com.haifeng.admin.vo.system.AdminLogListVO;

public interface AdminLogService {

    /**
     * 分页查询操作日志
     */
    IPage<AdminLogListVO> page(AdminLogQueryDTO dto);

    /**
     * 获取操作日志详情
     */
    AdminLogDetailVO detail(Long id);

    /**
     * 批量删除操作日志
     */
    int batchDelete(AdminLogBatchDeleteDTO dto);
}
