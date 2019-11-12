package com.github.yizzuide.milkomeda.test;

import com.github.yizzuide.milkomeda.demo.MilkomedaDemoApplication;
import com.github.yizzuide.milkomeda.echo.EchoRequest;
import com.github.yizzuide.milkomeda.echo.EchoResponseData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * EchoTest
 *
 * @author yizzuide
 * Create at 2019/11/12 17:19
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MilkomedaDemoApplication.class)
public class EchoTest {
    @Resource
    private EchoRequest simpleEchoRequest;

    @Test
    public void testOpenAccount() {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("uid", "1101");
        reqParams.put("name", "yiz");
        reqParams.put("id_no", "14324357894594483");

        // 发送请求，内部会进行签名
        // TypeReference比用xxx.class在泛型的支持上要强得多，IDE也会智能检测匹配成功
        // 如果第三方的data是一个json数组，可以传new TypeReference<List<Map<String, Object>>>() {}，返回结果用EchoResponseData<List<Map<String, Object>>>接收
//            EchoResponseData<Map<String, Object>> responseData = simpleEchoRequest.sendPostForResult("http://localhost:8091/echo/account/open", reqParams, new TypeReference<Map<String, Object>>() {}, true);
        EchoResponseData<Map<String, Object>> responseData = simpleEchoRequest.fetch(HttpMethod.POST, "http://localhost:8091/echo/account/open", reqParams);
        log.info("responseData: {}", responseData);
    }
}
