/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

import com.github.yizzuide.milkomeda.universe.lang.Tuple;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request mapping handle for 404, 406.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/07 18:49
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class UniformErrorController extends BasicErrorController {

    public UniformErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
    }

    public UniformErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if(UniformHandler.tryMatch(status.value())) {
            Tuple<HttpStatus, Map<String, Object>> mapTuple = uniformMatchResult(request, status);
            return ResponseEntity.status(mapTuple.getT1()).body(mapTuple.getT2());
        }
        return super.error(request);
    }

    // handle http content-type not match
    @Override
    public ResponseEntity<String> mediaTypeNotAcceptable(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if(UniformHandler.tryMatch(status.value())) {
            Tuple<HttpStatus, Map<String, Object>> mapTuple = uniformMatchResult(request, status);
            return ResponseEntity.status(mapTuple.getT1()).body(JSONUtil.serialize(mapTuple.getT2()));
        }
        return super.mediaTypeNotAcceptable(request);
    }

    @NotNull
    private Tuple<HttpStatus, Map<String, Object>> uniformMatchResult(HttpServletRequest request, HttpStatus status) {
        Map<String, Object> source = new HashMap<>();
        Map<String, Object> originalMap = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        String error = (String) originalMap.get("error");
        source.put(YmlResponseOutput.MESSAGE, error);
        Tuple<Map<String, Object>, Map<String, Object>> mapTuple = UniformHandler.matchStatusResult(status.value(), source);
        int resolveStatus = Integer.parseInt(mapTuple.getT1().get(YmlResponseOutput.STATUS).toString());
        Map<String, Object> body = mapTuple.getT2();
        return Tuple.build(HttpStatus.resolve(resolveStatus), body);
    }
}
