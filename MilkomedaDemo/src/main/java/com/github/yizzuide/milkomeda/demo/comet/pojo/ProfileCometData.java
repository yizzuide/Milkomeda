package com.github.yizzuide.milkomeda.demo.comet.pojo;

import com.github.yizzuide.milkomeda.comet.CometData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ProfileCometData
 *
 * @author yizzuide
 * Create at 2019/04/16 18:14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProfileCometData extends CometData {
    private String uid;
}
