package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.yml.YmlParser;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.*;

/**
 * UniformResponseExceptionHandler
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/25 22:47
 */
@Slf4j
@ControllerAdvice // 可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute, 并应用到所有@RequestMapping中
public class UniformExceptionResponseHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private HydrogenProperties props;
    /**
     * 自定义异常配置列表缓存
     */
    private List<Map<String, Object>> customConfList;

    // 4xx异常处理
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ResponseEntity<Object> responseEntity = handleExceptionResponse(ex, status.value(), ex.getMessage());
        if (responseEntity == null) {
            return super.handleExceptionInternal(ex, body, headers, status, request);
        }
        return responseEntity;
    }

    // 方法上单个普通类型（如：String、Long等）参数校验异常（校验注解直接写在参数前面的方式）
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationException(ConstraintViolationException e) {
        ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
        String value = String.valueOf(constraintViolation.getInvalidValue());
        String message = WebContext.getRequest().getRequestURI() +
                " [" + constraintViolation.getPropertyPath() + "=" + value + "] " + constraintViolation.getMessage();
        log.warn("Hydrogen uniform valid response exception with msg: {} ", message);
        return handleExceptionResponse(e, HttpStatus.BAD_REQUEST.value(), message);
    }

    // 对方法上@RequestBody的Bean参数校验的处理
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleValidBeanExceptionResponse(ex, ex.getBindingResult());
    }

    // 对方法的Form提交参数绑定校验的处理
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleValidBeanExceptionResponse(ex, ex.getBindingResult());
    }

    // 其它内部异常处理
    @SuppressWarnings("all")
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) throws IOException {
        Map<String, Object> body = props.getUniform().getBody();
        Object status = body.get("status");
        status = status == null ?  500 : status;
        Map<String, Object> result = new HashMap<>();

        // 自定义异常处理
        Object customs = body.get("customs");
        if (customs != null) {
            if (this.customConfList == null) {
                this.customConfList = new ArrayList<>();
                Map<String, Map<String, Object>> customsMap = (Map<String, Map<String, Object>>) customs;
                for (String k : customsMap.keySet()) {
                    Map<String, Object> eleMap = customsMap.get(k);
                    Object clazz = eleMap.get("clazz");
                    if (clazz == null) continue;
                    Class<?> expClazz = null;
                    try {
                        expClazz = Class.forName(clazz.toString());
                    } catch (Exception ex) {
                        log.error("Hydrogen load class error with msg: {}", ex.getMessage(), e);
                    }
                    eleMap.put("clazz", expClazz);
                    this.customConfList.add(eleMap);
                }
            }

            for (Map<String, Object> map : this.customConfList) {
                Class<Exception> exceptionClass = (Class<Exception>) map.get("clazz");
                if (exceptionClass.isInstance(e)) {
                    Map<String, Object> exMap = DataTypeConvertUtil.beanToMap(e);
                    YmlParser.parseAliasNodePath(map, result, "code", null, exMap);
                    YmlParser.parseAliasNodePath(map, result, "message", null, exMap);
                    // 其它自定义key也返回
                    map.keySet().stream().filter(k -> !Arrays.asList("clazz", "status", "code", "message").contains(k) && !result.containsKey(k))
                            .forEach(k ->  YmlParser.parseAliasNodePath(map, result, k, null, exMap));
                    return ResponseEntity.status(Integer.parseInt(status.toString())).body(result);
                }
            }
        }

        log.error("Hydrogen uniform response exception with msg: {}", e.getMessage(), e);
        YmlParser.parseAliasNodePath(body, result, "code", -1, null);
        YmlParser.parseAliasNodePath(body, result, "message", "服务器繁忙，请稍后再试！", null);
        YmlParser.parseAliasNodePath(body, result, "error-stack-msg", null, e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            String errorStack = String.format("exception happened: %s \n invoke root: %s", stackTrace[0], stackTrace[stackTrace.length - 1]);
            YmlParser.parseAliasNodePath(body, result, "error-stack", null, errorStack);
        }
        return ResponseEntity.status(Integer.parseInt(status.toString())).body(result);
    }

    /**
     * 处理Bean校验异常
     * @param ex            异常
     * @param bindingResult 错误绑定数据
     * @return  ResponseEntity
     */
    private ResponseEntity<Object> handleValidBeanExceptionResponse(Exception ex, BindingResult bindingResult) {
        ObjectError objectError = bindingResult.getAllErrors().get(0);
        String message = objectError.getDefaultMessage();
        if (objectError.getArguments() != null && objectError.getArguments().length > 0) {
            FieldError fieldError = (FieldError) objectError;
            message = WebContext.getRequest().getRequestURI() + " [" + fieldError.getField() + "=" + fieldError.getRejectedValue() + "] " + message;
        }
        log.warn("Hydrogen uniform valid response exception with msg: {} ", message);
        return handleExceptionResponse(ex, HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 处理非5xx异常响应
     * @param ex                异常
     * @param presetStatusCode  预设响应码
     * @param presetMessage     预设错误消息
     * @return  ResponseEntity
     */
    @SuppressWarnings("all")
    private ResponseEntity<Object> handleExceptionResponse(Exception ex, Object presetStatusCode, String presetMessage) {
        Map<String, Object> body = props.getUniform().getBody();
        Map<String, Object> result = new HashMap<>();
        Object exp4xx = body.get(presetStatusCode.toString());
        if (!(exp4xx instanceof Map)) {
            return null;
        }
        Map<String, Object> exp4xxBody = (Map<String, Object>) exp4xx;
        Object statusCode4xx = exp4xxBody.get("status");
        if (statusCode4xx == null || presetStatusCode.equals(statusCode4xx)) {
            presetStatusCode = statusCode4xx;
            return ResponseEntity.status(Integer.parseInt(presetStatusCode.toString())).body(null);
        }

        YmlParser.parseAliasNodePath(exp4xxBody, result, "code", presetStatusCode, presetStatusCode);
        YmlParser.parseAliasNodePath(exp4xxBody, result, "message", presetMessage, presetMessage);
        return ResponseEntity.status(Integer.parseInt(statusCode4xx.toString())).body(result);
    }
}
