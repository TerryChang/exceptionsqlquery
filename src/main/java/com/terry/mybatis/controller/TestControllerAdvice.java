package com.terry.mybatis.controller;

import com.terry.mybatis.mybatis.CustomDataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TestControllerAdvice {
    @ExceptionHandler(CustomDataAccessException.class)
    public String processCustomDataAccessException(CustomDataAccessException cdae){
        return cdae.getQuery();
    }
}
