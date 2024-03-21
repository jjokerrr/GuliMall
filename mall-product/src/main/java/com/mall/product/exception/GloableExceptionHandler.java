package com.mall.product.exception;

import com.mall.common.excption.ExceptionCode;
import com.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GloableExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleArgumentValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errMap = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return R.error(ExceptionCode.VALID_EXCEPTION_CODE.getCode(), ExceptionCode.VALID_EXCEPTION_CODE.getMsg()).put("data", errMap);


    }
}
