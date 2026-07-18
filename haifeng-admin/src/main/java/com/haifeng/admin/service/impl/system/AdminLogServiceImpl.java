package com.haifeng.admin.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.system.AdminLogBatchDeleteDTO;
import com.haifeng.admin.dto.system.AdminLogQueryDTO;
import com.haifeng.admin.service.system.AdminLogService;
import com.haifeng.admin.vo.system.AdminLogDetailVO;
import com.haifeng.admin.vo.system.AdminLogListVO;
import com.haifeng.common.entity.system.AdminLog;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.system.AdminLogMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl implements AdminLogService {

    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<AdminLogListVO> page(AdminLogQueryDTO dto) {
        Page<AdminLog> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<AdminLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getAdminName())) {
            wrapper.like(AdminLog::getAdminName, dto.getAdminName());
        }
        if (StringUtils.hasText(dto.getResult())) {
            wrapper.eq(AdminLog::getResult, dto.getResult());
        }
        if (StringUtils.hasText(dto.getRequestMethod())) {
            wrapper.eq(AdminLog::getRequestMethod, dto.getRequestMethod());
        }

        wrapper.orderByDesc(AdminLog::getCreatedAt);

        IPage<AdminLog> logPage = adminLogMapper.selectPage(page, wrapper);

        return logPage.convert(adminLog -> AdminLogListVO.builder()
                .id(adminLog.getId())
                .adminName(adminLog.getAdminName())
                .operation(adminLog.getOperation())
                .requestMethod(adminLog.getRequestMethod())
                .result(adminLog.getResult())
                .ip(adminLog.getIp())
                .createdAt(adminLog.getCreatedAt())
                .build());
    }

    @Override
    public AdminLogDetailVO detail(Long id) {
        AdminLog adminLog = adminLogMapper.selectById(id);
        if (adminLog == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "操作日志不存在");
        }

        String requestParams = adminLog.getRequestParams();
        if (StringUtils.hasText(requestParams) && requestParams.length() > 500) {
            requestParams = requestParams.substring(0, 500) + "...(已截断)";
        }

        return AdminLogDetailVO.builder()
                .id(adminLog.getId())
                .adminId(adminLog.getAdminId())
                .adminName(adminLog.getAdminName())
                .operation(adminLog.getOperation())
                .requestPath(adminLog.getRequestPath())
                .requestMethod(adminLog.getRequestMethod())
                .requestParams(requestParams)
                .result(adminLog.getResult())
                .errorMsg(adminLog.getErrorMsg())
                .ip(adminLog.getIp())
                .createdAt(adminLog.getCreatedAt())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelete(AdminLogBatchDeleteDTO dto) {
        String type = dto.getType();
        int deletedCount;

        switch (type) {
            case "ids":
                if (CollectionUtils.isEmpty(dto.getIds())) {
                    throw new BusinessException(400, "请选择要删除的记录");
                }
                deletedCount = adminLogMapper.batchHardDelete(dto.getIds());
                log.info("批量删除操作日志: type=ids, count={}", deletedCount);
                break;

            case "lastMonth":
                OffsetDateTime oneMonthAgo = OffsetDateTime.now().minusMonths(1);
                deletedCount = adminLogMapper.deleteBeforeTime(oneMonthAgo);
                log.info("删除一个月前的操作日志: type=lastMonth, count={}", deletedCount);
                break;

            case "all":
                long totalCount = adminLogMapper.selectCount(null);
                if (totalCount > 10000) {
                    throw new BusinessException(400, "日志总数超过10000条，请联系管理员处理");
                }
                deletedCount = adminLogMapper.deleteAll();
                log.info("删除全部操作日志: type=all, count={}", deletedCount);
                break;

            default:
                throw new BusinessException(400, "不支持的删除类型: " + type);
        }

        return deletedCount;
    }
}
