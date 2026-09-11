package org.example.springbootproject.config;

import org.example.springbootproject.pojo.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器
 * 统一处理系统异常，返回友好的错误信息
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.error("文件大小超过限制，单个文件最大支持100MB，请压缩文件后重新上传");
    }
}

