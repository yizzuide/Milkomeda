package com.github.yizzuide.milkomeda.demo.echo.request;

import com.github.yizzuide.milkomeda.echo.EchoResponseData;
import lombok.Data;

/**
 * EchoResponseData
 * 扩展响应类例子，如你对接的第三方响应字段不同，需要通过定义实现EchoResponseData接口即可扩展
 *
 * @author yizzuide
 * <br>
 * Create at 2019/09/21 18:56
 */
@Data
public class SimpleEchoResponseData<T> implements EchoResponseData<T> {
    // 以下字段作为对接一个第三方平台的例子，不一定适用你的情况
    private String code;
    private String errorMsg;
    private T data;

    // 如果参数的getter和接口定义不一致，可以这样适配一下，这样达到对外都是统一的
    @Override
    public String getMsg() {
        return errorMsg;
    }
}
