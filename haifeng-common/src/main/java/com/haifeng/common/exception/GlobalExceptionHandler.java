package com.haifeng.common.exception;

import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMsg());
        return R.fail(e.getCode(), e.getMsg());
    }

    /**
     * AI 配额超额 - 返回 HTTP 429
     */
    @ExceptionHandler(QuotaExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public R<Void> handleQuotaExceededException(QuotaExceededException e) {
        log.warn("AI 调用次数超额: {}", e.getMsg());
        return R.fail(e.getCode(), e.getMsg());
    }

    /**
     * 参数校验异常 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 参数校验异常 - @Validated
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 参数校验异常 - @Validated 单参数
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束校验失败: {}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return R.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 唯一约束冲突
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("唯一约束冲突: {}", e.getMostSpecificCause().getMessage());
        return R.fail(ResultCode.BAD_REQUEST.getCode(), "数据已存在");
    }

    /**
     * 授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝: {}", e.getMessage());
        return R.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 乐观锁冲突
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleOptimisticLockingFailureException(OptimisticLockingFailureException e) {
        log.warn("乐观锁冲突: {}", e.getMessage());
        return R.fail(ResultCode.BAD_REQUEST.getCode(), "数据已被其他人修改，请刷新后重试");
    }

    /**
     * 其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        // 禁止把堆栈信息返回给前端
        return R.fail(ResultCode.INTERNAL_ERROR);
    }
}
