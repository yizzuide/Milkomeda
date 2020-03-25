package com.github.yizzuide.milkomeda.hydrogen;

import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * UniformResponseExceptionHandler
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/25 22:47
 */
@Slf4j
@ControllerAdvice // 可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute, 并应用到所有@RequestMapping中
public class UniformResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private HydrogenProperties props;

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletResponse response) throws IOException {
        log.error("Hydrogen uniform response exception with msg:{} ", e.getMessage(), e);
        Map<String, Object> body = props.getResponseFrontException().getBody();
        Object status = body.get("status");
        response.setStatus(status == null ?  HttpStatus.OK.value() : Integer.parseInt(status.toString()));
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        putMapElement(body, result, "code", -1, null);
        putMapElement(body, result, "message", "服务器繁忙，请稍后再试！", null);
        putMapElement(body, result, "error-stack-msg", null, e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            String errorStack = String.format("exception happened: %s \n invoke root: %s", stackTrace[0], stackTrace[stackTrace.length - 1]);
            putMapElement(body, result, "error-stack", null, errorStack);
        }
        writer.println(JSONUtil.serialize(result));
        writer.flush();
    }

    /**
     * 添加响应字段
     * @param body          响应字段集
     * @param result        返回map
     * @param originKey     定制的key
     * @param defaultValue  默认的值
     * @param replace       如果值为""，使用替换的值
     */
    @SuppressWarnings("all")
    private void putMapElement(Map<String, Object> body, Map<String, Object> result, String originKey, Object defaultValue, String replace) {
        String errorStackMsgKey = originKey;
        Object errorStackMsgValue = body.get(originKey);
        if (errorStackMsgValue == null && defaultValue != null) {
            errorStackMsgValue = defaultValue;
        } else if (errorStackMsgValue instanceof Map) {
            Map<String, Object> errorStackMsgMap = (Map<String, Object>) errorStackMsgValue;
            errorStackMsgKey = String.valueOf(errorStackMsgMap.keySet().toArray()[0]);
            errorStackMsgValue = errorStackMsgMap.get(errorStackMsgKey);
        }
        if (errorStackMsgValue != null) {
            result.put(errorStackMsgKey, StringUtils.isEmpty(errorStackMsgValue) && replace != null ? replace : errorStackMsgValue);
        }
    }
}
