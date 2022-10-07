package com.github.yizzuide.milkomeda.demo.pillar.config;

import com.github.yizzuide.milkomeda.demo.pillar.common.ReturnData;
import com.github.yizzuide.milkomeda.pillar.Pillar;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * PillarConfig
 *
 * @author yizzuide
 * <br />
 * Create at 2019/05/25 21:21
 */
@Configuration
public class PillarConfig {

    @Autowired
    private List<Pillar<Map<String, String>, ReturnData>> pillars;

    @Bean
    public PillarExecutor<Map<String, String>, ReturnData> pillarExecutor() {
        PillarExecutor<Map<String, String>, ReturnData> pillarExecutor = new PillarExecutor<>();
        pillarExecutor.addPillarList(pillars);
        return pillarExecutor;
    }
}
