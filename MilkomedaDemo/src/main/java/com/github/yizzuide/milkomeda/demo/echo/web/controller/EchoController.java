package com.github.yizzuide.milkomeda.demo.echo.web.controller;

import com.github.yizzuide.milkomeda.comet.core.CometParam;
import com.github.yizzuide.milkomeda.demo.echo.request.SimpleEchoRequest;
import com.github.yizzuide.milkomeda.echo.EchoException;
import com.github.yizzuide.milkomeda.echo.EchoRequest;
import com.github.yizzuide.milkomeda.echo.EchoResponseData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * EchoController
 *
 * @author yizzuide
 * <br />
 * Create at 2019/09/21 20:18
 */
@Slf4j
@RestController
public class EchoController {

    @Resource
    private EchoRequest simpleEchoRequest;

    // 模拟一个第三方平台开户接口
    @RequestMapping("/echo/account/open")
    public ResponseEntity<Map<String, Object>> openAccount(
            @CometParam(decrypt = SimpleEchoRequest.class) Map<String, Object> params, HttpServletRequest request) {
        log.info("header: {}", request.getHeader("Authorization"));
        log.info("接收调用方参数：{}", params);

        // 第三方平台根据我们提供的公钥验签（如果是第三方平台回调我们的接口验签需要使用第三方提供的公钥，验签方式相同）
        // 当前验签功能由@CometParam(decrypt = SimpleEchoRequest.class)代替
        //Map<String, Object> map = simpleEchoRequest.verifyParam(params);

        Map<String, Object> data = new HashMap<>();
        if (params == null) {
            data.put("code", "403");
            data.put("error_msg", "验签失败");
            data.put("data", null);
        } else {
            data.put("code", "200");
            data.put("error_msg", "成功");
            data.put("data", "{\"order_id\":\"12343243434324324\"}");
        }
        return ResponseEntity.ok(data);
    }

    // 请求第三方平台开户接口
    @RequestMapping("/test/echo/account/open")
    public ResponseEntity<Map<String, Object>> requestOpenAccount() {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("uid", "1101");
        reqParams.put("name", "yiz");
        reqParams.put("id_no", "14324357894594483");

        try {
            // 发送请求，内部会进行签名
            // TypeReference比用xxx.class在泛型的支持上要强得多，IDE也会智能检测匹配成功
            // 如果第三方的data是一个json数组，可以传new TypeReference<List<Map<String, Object>>>() {}，返回结果用EchoResponseData<List<Map<String, Object>>>接收
//            EchoResponseData<Map<String, Object>> responseData = simpleEchoRequest.sendPostForResult("http://localhost:8091/echo/account/open", reqParams, new TypeReference<Map<String, Object>>() {}, true);
            EchoResponseData<Map<String, Object>> responseData = simpleEchoRequest.fetch(HttpMethod.POST, "http://localhost:8091/echo/account/open", null, reqParams, true);
            // 支持自定义业务类
            //EchoResponseData<PayVo> responseData = simpleEchoRequest.sendRequest(HttpMethod.POST, "http://localhost:8091/echo/account/open", null, reqParams, new TypeReference<PayVo>(){}, true);
            log.info("responseData: {}", responseData);
        } catch (EchoException e) {
            log.error("请求第三方开户接口出错：{}", e.getMessage());
            // 出错的业务处理。。。
        }

        Map<String, Object> data = new HashMap<>();
        data.put("code", "200");
        data.put("error_msg", "");
        return ResponseEntity.ok(data);
    }

    @Data
    static class PayVo {
        private String orderId;
    }
}
