package com.haifeng.admin.service.system;

import com.haifeng.admin.vo.system.AiBalanceVO;

import java.util.List;

/**
 * AI 厂商余额查询服务
 */
public interface AiBalanceService {

    /**
     * 查询 DeepSeek 厂商余额
     *
     * @param refresh true 时跳过缓存直接调 API
     * @return 按 apiKey 去重后的余额列表
     */
    List<AiBalanceVO> getDeepSeekBalances(boolean refresh);
}
