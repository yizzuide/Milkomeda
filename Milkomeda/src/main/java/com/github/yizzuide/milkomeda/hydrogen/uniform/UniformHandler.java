/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.lang.Tuple;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlParser;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * UniformHandler
 *
 * @see org.springframework.boot.SpringApplication#run(java.lang.String...)
 * #see org.springframework.boot.SpringApplication#registerLoggedException(java.lang.Throwable)
 * #see org.springframework.boot.SpringBootExceptionHandler.LoggedExceptionHandlerThreadLocal#initialValue()
 * @see org.springframework.boot.SpringApplication#setRegisterShutdownHook(boolean)
 * @see org.springframework.context.support.AbstractApplicationContext#registerShutdownHook()
 * @author yizzuide
 * @since 3.0.0
 * @version 4.0.0
 * <br>
 * Create at 2020/03/25 22:47
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
// 可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute, 并应用到所有@RequestMapping中
//@ControllerAdvice // 这种方式默认就会扫描并加载到Ioc，不好动态控制是否加载，但好处是外部API对未来版本的兼容性强
public class UniformHandler extends ResponseEntityExceptionHandler {

    public static final int REQUEST_BEFORE_EXCEPTION_CODE = 5000;

    @Autowired
    private UniformProperties props;

    /**
     * 自定义异常列表
     */
    private List<Map<String, Object>> customExpClazzList;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        // 初始化自定义异常
        Object customs = props.getResponse().get(YmlResponseOutput.CUSTOMS);
        if (customs == null) {
            return;
        }
        this.customExpClazzList = new ArrayList<>();
        Map<String, Map<String, Object>> customsMap = (Map<String, Map<String, Object>>) customs;
        for (String k : customsMap.keySet()) {
            Map<String, Object> configNodeMap = customsMap.get(k);
            Object clazzComposite = configNodeMap.get(YmlResponseOutput.CLAZZ);
            // clazz -> clazz list
            ArrayList<Class<?>> expClazzList = new ArrayList<>();
            if (clazzComposite instanceof Map) {
                Map<String, Object> clazzCompositeIndexMap = (Map<String, Object>) clazzComposite;
                for (String index : clazzCompositeIndexMap.keySet()) {
                    Class<?> expClazz = createExceptionClass(clazzCompositeIndexMap.get(index));
                    expClazzList.add(expClazz);
                }
            } else {
                Class<?> expClazz = createExceptionClass(clazzComposite);
                if (expClazz != null) {
                    expClazzList.add(expClazz);
                }
            }
            configNodeMap.put(YmlResponseOutput.CLAZZ, expClazzList);
            this.customExpClazzList.add(configNodeMap);
        }
    }

    private Class<?> createExceptionClass(Object clazz) {
        if (!(clazz instanceof String)) {
            return null;
        }

        Class<?> expClazz = null;
        try {
            // 在使用spring-boot-devtools时，业务类的类加载器为RestartClassLoader，这里配置的类就必须通过Thread.currentThread().getContextClassLoader()获取类加载器
            expClazz = Thread.currentThread().getContextClassLoader().loadClass(clazz.toString());
        } catch (Exception ex) {
            log.error("Hydrogen load class error with msg: {}", ex.getMessage(), ex);
        }
        return expClazz;
    }

    // 4xx异常处理
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NotNull Exception ex, @Nullable Object body, @NotNull HttpHeaders headers, @NotNull HttpStatusCode statusCode, @NotNull WebRequest request) {
        ResponseEntity<Object> responseEntity = handleExceptionResponse(ex, statusCode.value(), ex.getMessage());
        if (responseEntity == null) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }
        return responseEntity;
    }

    // 方法上单个普通类型（如：String、Long等）参数校验异常（校验注解直接写在参数前面的方式）
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> constraintViolationException(ConstraintViolationException e) {
        ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
        String value = String.valueOf(constraintViolation.getInvalidValue());
        String message;
        if (props.isIgnoreAddFieldOnValidFail()) {
            message = constraintViolation.getMessage();
        } else {
            message = "[" + constraintViolation.getPropertyPath() + "=" + value + "] " + constraintViolation.getMessage();
        }
        log.warn("Hydrogen uniform valid response exception with msg: {} ", message);
        ResponseEntity<Object> responseEntity = handleExceptionResponse(e, HttpStatus.BAD_REQUEST.value(), message);
        return responseEntity == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(null) : responseEntity;
    }

    // 对方法上@RequestBody的Bean和@RequestParam的Form参数校验的处理
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {
        ResponseEntity<Object> responseEntity = handleValidBeanExceptionResponse(ex, ex.getBindingResult());
        return responseEntity == null ? super.handleMethodArgumentNotValid(ex, headers, statusCode, request) : responseEntity;
    }

    // 其它内部异常处理
    @SuppressWarnings("unchecked")
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleException(Throwable e) {
        Map<String, Object> response = props.getResponse();
        Object status = response.get(YmlResponseOutput.STATUS);
        status = status == null ?  500 : status;
        Map<String, Object> result = new HashMap<>();

        // 查找自定义异常处理
        if (this.customExpClazzList != null) {
            for (Map<String, Object> map : this.customExpClazzList) {
                List<Class<Exception>> expClazzList = (List<Class<Exception>>) map.get(YmlResponseOutput.CLAZZ);
                if (expClazzList.stream().anyMatch(expClazz -> expClazz.getName().equals(e.getClass().getName()) || expClazz.isInstance(e))) {
                    YmlResponseOutput.output(map, result, null, (Exception) e, true);
                    return ResponseEntity.status(Integer.parseInt(status.toString())).body(result);
                }
            }
        }

        // 500异常
       return handleInnerErrorExceptionResponse((Exception) e, response, status.toString());
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
        if (!props.isIgnoreAddFieldOnValidFail()) {
            if (objectError.getArguments() != null && objectError.getArguments().length > 0) {
                FieldError fieldError = (FieldError) objectError;
                message = "[" + fieldError.getField() + "=" + fieldError.getRejectedValue() + "] " + message;
            }
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
    private @Nullable ResponseEntity<Object> handleExceptionResponse(Exception ex, Object presetStatusCode, String presetMessage) {
        Map<String, Object> response = props.getResponse();
        Map<String, Object> result = new HashMap<>();
        String code = presetStatusCode.toString();

        // 返回参数类型错误，这里会走500
        if (Objects.equals(code, "500")) {
            return handleInnerErrorExceptionResponse(ex, response, code);
        }

        Object exp4xx = response.get(code);
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

    // 500异常
    private ResponseEntity<Object> handleInnerErrorExceptionResponse(Exception ex, Map<String, Object> response,  String presetStatusCode) {
        Map<String, Object> result = new HashMap<>();
        log.error("Hydrogen uniform response exception with msg: {}", ex.getMessage(), ex);
        YmlResponseOutput.output(response, result, null, ex, false);
        return ResponseEntity.status(Integer.parseInt(presetStatusCode)).body(result);
    }

    /**
     * Try match response resolve with code before write.
     * @param code  response status code
     * @return  true if matched
     * @since 3.15.0
     */
    public static boolean tryMatch(int code) {
        BindResult<UniformProperties> bindResult = Binder.get(ApplicationContextHolder.get().getEnvironment()).bind(UniformProperties.PREFIX, UniformProperties.class);
        if (!bindResult.isBound()) {
            return false;
        }
        UniformProperties props = bindResult.get();
        Map<?, ?> resolveMap = (Map<?, ?>) props.getResponse().get(String.valueOf(code));
        return resolveMap != null;
    }

    /**
     * Used for external match with status code to get the response result.
     * @param statusCode  response status
     * @param source    replace data
     * @return tuple(yml node map, response content)
     * @since 3.15.0
     */
    @SuppressWarnings("unchecked")
    public static Tuple<Map<String, Object>, Map<String, Object>> matchStatusResult(int statusCode, Map<String, Object> source) {
        BindResult<UniformProperties> bindResult = Binder.get(ApplicationContextHolder.get().getEnvironment()).bind(UniformProperties.PREFIX, UniformProperties.class);
        Map<String, Object> resolveMap;
        if (bindResult.isBound()) {
            UniformProperties props = bindResult.get();
            resolveMap = (Map<String, Object>) props.getResponse().get(String.valueOf(statusCode));
            if (resolveMap == null) {
                resolveMap = createInitResolveMap();
            }
        } else {
            resolveMap = createInitResolveMap();
        }

        Map<String, Object> result = new HashMap<>();
        // status == 200?
        if (statusCode == HttpStatus.OK.value()) {
            YmlParser.parseAliasMapPath(resolveMap, result, YmlResponseOutput.CODE, null, source);
            YmlParser.parseAliasMapPath(resolveMap, result, YmlResponseOutput.MESSAGE, null, source);
            YmlParser.parseAliasMapPath(resolveMap, result, YmlResponseOutput.DATA, null, source);
            resultFilter(result);
        } else { // status != 200
            // 源Code字段为空或已配置了Code的值，就使用配置的值
            Object code = source.get(YmlResponseOutput.CODE);
            Object configCode = resolveMap.get(YmlResponseOutput.CODE);
            if (code == null || (configCode != null && StringUtils.hasText(configCode.toString()))) {
                source.put(YmlResponseOutput.CODE, resolveMap.get(YmlResponseOutput.CODE));
            }
            YmlResponseOutput.output(resolveMap, result, source, null, false);
        }
        return Tuple.build(resolveMap, result);
    }

    /**
     * Used for external match with status code to get the response result.
     * @param response  response object
     * @param source    replace data
     * @return tuple(yml node map, response content)
     * @since 3.14.0
     */

    public static Tuple<Map<String, Object>, Map<String, Object>> matchStatusResult(HttpServletResponse response, Map<String, Object> source) {
        return matchStatusResult(response.getStatus(), source);
    }

    @NotNull
    private static Map<String, Object> createInitResolveMap() {
        Map<String, Object> resolveMap = new HashMap<>(8);
        resolveMap.put(YmlResponseOutput.STATUS, HttpStatus.OK.value());
        resolveMap.put(YmlResponseOutput.CODE, "0");
        resolveMap.put(YmlResponseOutput.MESSAGE, "");
        resolveMap.put(YmlResponseOutput.DATA, Collections.emptyMap());
        return resolveMap;
    }

    /**
     * Used for external match with status code to write.
     * @param response  response object
     * @param status    http status code
     * @param e         exception
     * @throws IOException if an input or output exception occurred
     * @since 3.15.0
     */
    public static void matchStatusToWrite(HttpServletResponse response, Integer status, Exception e) throws IOException {
        response.setStatus(status == null ? REQUEST_BEFORE_EXCEPTION_CODE : status);
        ResultVO<?> source;
        if (e != null) {
            Map<String, Object> exMap = DataTypeConvertUtil.beanToMap(e);
            Object code = exMap.get(YmlResponseOutput.CODE);
            source = UniformResult.error(String.valueOf(code != null ? code : response.getStatus()), e.getMessage());
        } else {
            source = UniformResult.error(String.valueOf(response.getStatus()), "");
        }
        UniformHandler.matchStatusToWrite(response, source.toMap());
    }

    /**
     * Used for external match with status code to write.
     * @param response  response object
     * @param source    replace data
     * @throws IOException  if an input or output exception occurred
     * @since 3.14.0
     */
    public static void matchStatusToWrite(HttpServletResponse response, Map<String, Object> source) throws IOException {
        Tuple<Map<String, Object>, Map<String, Object>> mapTuple = matchStatusResult(response, source);
        Map<String, Object> resolveMap = mapTuple.getT1();
        Map<String, Object> result = mapTuple.getT2();
        if (mapTuple.getT1() == null || mapTuple.getT1().isEmpty()) {
            return;
        }
        String body = JSONUtil.serialize(result);
        Object statusCode = resolveMap.get(YmlResponseOutput.STATUS);
        if (statusCode != null) {
            response.setStatus(Integer.parseInt(statusCode.toString()));
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setContentLength(body.length());
        PrintWriter writer = response.getWriter();
        writer.println(body);
        writer.flush();
        writer.close();
    }

    /**
     * Force code type with config <code>milkomeda.hydrogen.uniform.code-type</code>
     * @param result response result map
     * @since 3.14.0
     */
    public static void resultFilter(Map<String, Object> result) {
        ResultVO.CodeType codeType = UniformHolder.getProps().getCodeType();
        Object code = result.get(YmlResponseOutput.CODE);
        if (code != null) {
            if (codeType == ResultVO.CodeType.INT) {
                code = Integer.parseInt(code.toString());
            } else {
                code = code.toString();
            }
            result.put(YmlResponseOutput.CODE, code);
        }
    }
}
