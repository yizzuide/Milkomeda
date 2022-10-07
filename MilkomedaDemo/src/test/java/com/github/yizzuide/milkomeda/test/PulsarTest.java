package com.github.yizzuide.milkomeda.test;

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
import com.github.yizzuide.milkomeda.demo.MilkomedaDemoApplication;

/**
 * PulsarTest
 *
 * @author yizzuide
 * <br />
 * Create at 2019/03/30 19:29
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MilkomedaDemoApplication.class)
public class PulsarTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void login() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/user/login")
                .param("username", "yiz")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getAsyncResult();
        System.out.println(ret);
    }

    @Test
    public void userInfo() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/user/1")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getAsyncResult();
        System.out.println(ret);
    }

    @Test
    public void sendNotice() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/user/notice/1")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()

                // 同步请求
//                .getResponse()
//                .getContentAsString();

                // 异步请求
                .getAsyncResult();
        System.out.println(ret);
    }
}
