package com.github.yizzuide.milkomeda.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.demo.MilkomedaDemoApplication;
import com.github.yizzuide.milkomeda.echo.EchoResponseData;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.yizzuide.milkomeda.util.JSONUtil.toCamel;

/**
 * UtilTest
 *
 * @author yizzuide
 * <br />
 * Create at 2019/09/21 18:23
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MilkomedaDemoApplication.class)
public class UtilTest {
    @Test
    public void testCamelConvert() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("error_msg", "成功");
        map.put("data", Arrays.asList(new HashMap<String, Object>(){{
            put("user_name", "yiz1");
            put("id_no", "111111");
        }}, new HashMap<String, Object>(){{
            put("user_name", "yiz2");
            put("id_no", "222222");
        }}));
        TestEchoResponseData<List<Map>> data = toCamel(map, new TypeReference<TestEchoResponseData<List<Map>>>() {});
        System.out.println(data);
    }

    @Data
    static class TestEchoResponseData<T> implements EchoResponseData<T> {
        private String code;
        private String errorMsg;
        private T data;

        @Override
        public String getMsg() {
            return errorMsg;
        }
    }
}
