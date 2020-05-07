package com.github.yizzuide.milkomeda.test;

import com.github.yizzuide.milkomeda.demo.MilkomedaDemoApplication;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

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
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testOpenAccount() throws Exception {
        MultiValueMap<String, String> reqParams = new LinkedMultiValueMap<>();
        reqParams.put("uid", Collections.singletonList("1101"));
        reqParams.put("name", Collections.singletonList("yiz"));
        reqParams.put("id_no", Collections.singletonList("14324357894594483"));

        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/echo/account/open")
                .params(reqParams)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }
}
