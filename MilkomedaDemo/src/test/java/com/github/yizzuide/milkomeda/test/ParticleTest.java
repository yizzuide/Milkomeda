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
 * ParticleTest
 *
 * @author yizzuide
 * Create at 2019/05/31 14:51
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MilkomedaDemoApplication.class)
public class ParticleTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void check() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/particle/check")
                .param("token", "123321")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }

    @Test
    public void check2() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/particle/check2")
                .param("token", "123321")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }

    @Test
    public void check3() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/particle/check3")
                .header("X-Token", "123321")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }

    @Test
    public void send() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/particle/send")
                .param("phone", "151")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }

    @Test
    public void send2() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/particle/send2")
                .param("phone", "151")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }

    @Test
    public void verify() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/particle/verify")
                .param("phone", "151")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }

    @Test
    public void verify2() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/particle/verify2")
                .param("phone", "151")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }
}
