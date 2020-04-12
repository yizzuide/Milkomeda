package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
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

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UniformHandler
 *
 * @author yizzuide
 * @since 3.0.0
 * @see org.springframework.boot.SpringApplication#run(java.lang.String...)
 * #see org.springframework.boot.SpringApplication#registerLoggedException(java.lang.Throwable)
 * #see org.springframework.boot.SpringBootExceptionHandler.LoggedExceptionHandlerThreadLocal#initialValue()
 * @see org.springframework.boot.SpringApplication#setRegisterShutdownHook(boolean)
 * @see org.springframework.context.support.AbstractApplicationContext#registerShutdownHook()
 * Create at 2020/03/25 22:47
 */
@Slf4j
@ControllerAdvice // 可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute, 并应用到所有@RequestMapping中
public class UniformHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private UniformProperties props;

    /**
     * 自定义异常配置列表缓存
     */
    private List<Map<String, Object>> customConfList;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        // 初始化自定义异常
        Object customs = props.getResponse().get(YmlResponseOutput.CUSTOMS);
        if (customs == null) {
            return;
        }
        this.customConfList = new ArrayList<>();
        Map<String, Map<String, Object>> customsMap = (Map<String, Map<String, Object>>) customs;
        for (String k : customsMap.keySet()) {
            Map<String, Object> eleMap = customsMap.get(k);
            Object clazz = eleMap.get(YmlResponseOutput.CLAZZ);
            if (!(clazz instanceof String)) continue;
            Class<?> expClazz;
            try {
                expClazz = Class.forName(clazz.toString());
            } catch (Exception ex) {
                log.error("Hydrogen load class error with msg: {}", ex.getMessage(), ex);
                continue;
            }
            eleMap.put(YmlResponseOutput.CLAZZ, expClazz);
            this.customConfList.add(eleMap);
        }
    }

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
        ResponseEntity<Object> responseEntity = handleExceptionResponse(e, HttpStatus.BAD_REQUEST.value(), message);
        return responseEntity == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(null) : responseEntity;
    }

    // 对方法上@RequestBody的Bean参数校验的处理
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ResponseEntity<Object> responseEntity = handleValidBeanExceptionResponse(ex, ex.getBindingResult());
        return responseEntity == null ? super.handleMethodArgumentNotValid(ex, headers, status, request) : responseEntity;
    }

    // 对方法的Form提交参数绑定校验的处理
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ResponseEntity<Object> responseEntity = handleValidBeanExceptionResponse(ex, ex.getBindingResult());
        return responseEntity == null ? super.handleBindException(ex, headers, status, request) : responseEntity;
    }

    // 其它内部异常处理
    @SuppressWarnings("unchecked")
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) throws IOException {
        Map<String, Object> response = props.getResponse();
        Object status = response.get(YmlResponseOutput.STATUS);
        status = status == null ?  500 : status;
        Map<String, Object> result = new HashMap<>();

        // 查找自定义异常处理
        if (this.customConfList != null) {
            for (Map<String, Object> map : this.customConfList) {
                Class<Exception> exceptionClass = (Class<Exception>) map.get(YmlResponseOutput.CLAZZ);
                if (exceptionClass.isInstance(e)) {
                    YmlResponseOutput.output(map, result, null, e, true);
                    return ResponseEntity.status(Integer.parseInt(status.toString())).body(result);
                }
            }
        }

        // 500异常
        log.error("Hydrogen uniform response exception with msg: {}", e.getMessage(), e);
        YmlResponseOutput.output(response, result, null, e, false);
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
    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> handleExceptionResponse(Exception ex, Object presetStatusCode, String presetMessage) {
        Map<String, Object> response = props.getResponse();
        Map<String, Object> result = new HashMap<>();
        Object exp4xx = response.get(presetStatusCode.toString());
        if (!(exp4xx instanceof Map)) {
            log.warn("Hydrogen uniform can't find {} code response.", presetStatusCode);
            // 调用方判断，按框架默认处理
            return null;
        }

        Map<String, Object> exp4xxResponse = (Map<String, Object>) exp4xx;
        Object statusCode4xx = exp4xxResponse.get(YmlResponseOutput.STATUS);
        if (statusCode4xx == null || presetStatusCode.equals(statusCode4xx)) {
            return ResponseEntity.status(Integer.parseInt(presetStatusCode.toString())).body(null);
        }
        Map<String, Object> defValMap = new HashMap<>();
        defValMap.put(YmlResponseOutput.CODE, presetStatusCode);
        defValMap.put(YmlResponseOutput.MESSAGE, presetMessage);
        YmlResponseOutput.output(exp4xxResponse, result, defValMap, null, false);
        return ResponseEntity.status(Integer.parseInt(statusCode4xx.toString())).body(result);
    }
}
