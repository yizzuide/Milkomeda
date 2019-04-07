package com.github.yizzuide.milkomeda.demo.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.github.yizzuide.milkomeda.demo.exception.YizException;

import java.util.HashMap;
import java.util.Map;

/**
 * RequestExceptionHandler
 * 请求错误处理器
 *
 * @author yizzuide
 * Create at 2018-12-17 15:41
 */
@Slf4j
@ControllerAdvice // 可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute, 并应用到所有@RequestMapping中
public class RequestExceptionHandler extends ResponseEntityExceptionHandler {

    /** 处理自定义异常 */
    @ExceptionHandler(YizException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置响应状态码为500
    public Map<String, Object> handleException(YizException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", e.getId());
        result.put("type", e.getType());
        result.put("message", e.getMessage());
        return result;
    }

    /** 其它异常 */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception e) {
        log.error("invoke error", e);
        Map<String, Object> ret = new HashMap<>();
        ret.put("msg", e.getMessage());
        return ret;
    }



}
