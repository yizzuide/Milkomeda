package com.github.yizzuide.milkomeda.test;

import com.github.yizzuide.milkomeda.demo.MilkomedaDemoApplication;
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
import org.springframework.web.context.WebApplicationContext;

/**
 * CometTest
 *
 * @author yizzuide
 * Create at 2019/04/11 23:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MilkomedaDemoApplication.class)
public class CometTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void feature() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/collect/feature")
                .param("uuid", "1123458897655")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }
}
