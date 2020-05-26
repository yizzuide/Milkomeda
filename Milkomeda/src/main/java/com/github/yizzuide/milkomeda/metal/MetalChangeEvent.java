package com.github.yizzuide.milkomeda.metal;

import org.springframework.context.ApplicationEvent;

/**
 * MetalChangeEvent
 *
 * @author yizzuide
 * @since 3.6.2
 * Create at 2020/05/26 15:29
 */
public class MetalChangeEvent extends ApplicationEvent {
    private static final long serialVersionUID = -8409365025025128964L;
    private String key;
    private String oldVal;
    private String newVal;

    public MetalChangeEvent(Object source, String key, String oldVal, String newVal) {
        super(source);
        this.key = key;
        this.oldVal = oldVal;
        this.newVal = newVal;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOldVal() {
        return oldVal;
    }

    public void setOldVal(String oldVal) {
        this.oldVal = oldVal;
    }

    public String getNewVal() {
        return newVal;
    }

    public void setNewVal(String newVal) {
        this.newVal = newVal;
    }
}
