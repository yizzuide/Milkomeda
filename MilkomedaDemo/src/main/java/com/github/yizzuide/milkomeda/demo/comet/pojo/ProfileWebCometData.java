package com.github.yizzuide.milkomeda.demo.comet.pojo;

import com.github.yizzuide.milkomeda.comet.core.WebCometData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * ProfileCometData
 *
 * @author yizzuide
 * <br />
 * Create at 2019/04/16 18:14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProfileWebCometData extends WebCometData {
    private static final long serialVersionUID = 7001588685766209866L;
    private String uid;
}
