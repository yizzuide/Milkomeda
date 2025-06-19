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
 * <br>
 * Create at 2019/04/11 23:02
 */
@RunWith(SpringRunner.class)
// Spring Boot 3.0: The @SpringBootTest annotation can now use the main of any discovered @SpringBootConfiguration class if it’s available.
//  This means that any custom SpringApplication configuration performed by your main method can now be picked up by tests.
// To use the main method for a test set the useMainMethod attribute of @SpringBootTest to UseMainMethod.ALWAYS or UseMainMethod.WHEN_AVAILABLE
@SpringBootTest(classes = MilkomedaDemoApplication.class, useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE)
public class CometTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    // Spring Boot 3.1: MockServerRestTemplateCustomizer now supports enable content buffering through a new setBufferContent method.
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void feature() throws Exception {
        val ret = mockMvc.perform(MockMvcRequestBuilders.get("/collect/product/click")
                .param("productId", "1000")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(ret);
    }
}
