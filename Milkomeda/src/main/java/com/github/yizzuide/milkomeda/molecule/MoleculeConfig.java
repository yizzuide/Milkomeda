/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.molecule;

import com.github.yizzuide.milkomeda.molecule.core.event.DomainEventBus;
import com.github.yizzuide.milkomeda.molecule.core.event.DomainEventPublisher;
import com.github.yizzuide.milkomeda.molecule.core.event.SpringApplicationDomainEventPublisher;
import com.github.yizzuide.milkomeda.molecule.core.eventhandler.DefaultEventHandler;
import com.github.yizzuide.milkomeda.orbit.OrbitConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Molecule module configuration.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/09 16:47
 */
@Import(OrbitConfig.class)
@Configuration
public class MoleculeConfig {

    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return new SpringApplicationDomainEventPublisher();
    }

    @Bean
    public DomainEventBus domainEventBus(DomainEventPublisher domainEventPublisher) {
        DomainEventBus domainEventBus = new DomainEventBus(domainEventPublisher);
        MoleculeContext.setDomainEventBus(domainEventBus);
        return domainEventBus;
    }

    @Bean
    public DefaultEventHandler defaultEventHandler() {
        return new DefaultEventHandler();
    }
}
