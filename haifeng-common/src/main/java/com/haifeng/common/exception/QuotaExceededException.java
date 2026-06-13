package com.haifeng.common.exception;

import com.haifeng.common.response.ResultCode;

/**
 * 当日 AI 调用次数超额异常。
 * 由 GlobalExceptionHandler 映射为 HTTP 429。
 */
public class QuotaExceededException extends BusinessException {

    public QuotaExceededException() {
        super(ResultCode.AI_QUOTA_EXCEEDED);
    }
}
