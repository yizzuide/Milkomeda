package com.github.yizzuide.milkomeda.universe.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringUtils;

/**
 * ConditionPropertySource
 *
 * @author yizzuide
 * @version 3.2.1
 * Create at 2020/04/29 15:30
 */
@Slf4j
public class ConditionPropertySource extends PropertySource<Object> {

    public static final String CONDITION_PROPERTY_SOURCE_NAME = "condition";

    private static final String PREFIX = "condition.";

    public ConditionPropertySource() {
        super(CONDITION_PROPERTY_SOURCE_NAME);
    }

    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        return getConditionValue(name.substring(PREFIX.length()));
    }

    private Object getConditionValue(String type) {
        String range = getRange(type, "equals");
        if (range != null) {
            return getBoolValue(range);
        }
        range = getRange(type, "diff");
        if (range != null) {
            return !getBoolValue(range);
        }
        return null;
    }

    private boolean getBoolValue(String range) {
        range = StringUtils.trimAllWhitespace(range);
        String[] parts = StringUtils.commaDelimitedListToStringArray(range);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Condition.bool set args error.");
        }
        return parts[0].equals(parts[1]);
    }

    private String getRange(String type, String prefix) {
        if (type.startsWith(prefix)) {
            int startIndex = prefix.length() + 1;
            if (type.length() > startIndex) {
                return type.substring(startIndex, type.length() - 1);
            }
        }
        return null;
    }

    public static void addToEnvironment(ConfigurableEnvironment environment) {
        environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new ConditionPropertySource());
        log.trace("CollectionsPropertySource add to Environment");
    }
}
