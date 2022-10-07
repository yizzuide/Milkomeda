package com.github.yizzuide.milkomeda.demo.echo.request;

import com.github.yizzuide.milkomeda.demo.echo.props.ThirdKey;
import com.github.yizzuide.milkomeda.echo.EchoException;
import com.github.yizzuide.milkomeda.echo.EchoRequest;
import com.github.yizzuide.milkomeda.echo.EchoResponseData;
import com.github.yizzuide.milkomeda.echo.ErrorCode;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.EncryptUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SimpleEchoRequest
 * 扩展EchoRequest实现当前对接的第三方响应类型
 *
 * @author yizzuide
 * <br />
 * Create at 2019/09/21 20:35
 */
@Slf4j
@Component
public class SimpleEchoRequest extends EchoRequest {

    @Autowired
    private ThirdKey thirdKey;

    @Override
    protected <T> EchoResponseData<T> responseData() {
        return new SimpleEchoResponseData<>();
    }

    @Override
    protected void checkResponse(EchoResponseData responseData) throws EchoException {
        // 检测第三方返回的code是否ok
        if (!("200".equals(responseData.getCode()))) {
            log.error("SimpleEchoRequest:- response error with msg: {}, code:{}", responseData.getMsg(), responseData.getCode());
            // 抛出异常，在请求代码处可以捕获
            throw new EchoException(ErrorCode.VENDOR_RESPONSE_IS_FAIL, responseData.getMsg());
        }
    }


    @Override
    protected void appendHeaders(HttpHeaders headers) {
        // 默认使用application/json
        super.appendHeaders(headers);

        // 如果使用application/x-www-form-urlencoded
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 自定义请求头
//        headers.add("Authorization", "Basic c2tfdGVzdF9PWUNFZkw1eGdMTCtsOFp2SzdRYVNHR1ZNTjczb05FcGszeXorUnhuLzJiUy9MQ2dDUVorZ3c9PTo=");
    }

    // 添加统一传递参数，如appId、签名等，如是不需要可不用覆盖这个类
    @Override
//    @SuppressWarnings("unchecked")
    protected void signParam(Map<String, Object> inParams, Map<String, Object> outParams) {
        // 父类实现直接将 inParams 数据给到 outParams
//        super.signParam(inParams, outParams);

        // 如果设置了MediaType.APPLICATION_FORM_URLENCODED，则outParams为LinkedMultiValueMap
        /*if (outParams instanceof LinkedMultiValueMap) {
            LinkedMultiValueMap multiValueMap = (LinkedMultiValueMap) outParams;
            multiValueMap.add("appId", "1000");
            multiValueMap.add("timestamp", "2019-09-21 17:12:00");
            multiValueMap.add("version", "1.0");
            // ...
        }*/

        // 下面是签名的例子
        outParams.put("appId", "1000");
        outParams.put("version", "1.0");
        DataTypeConvertUtil.clearEmptyValue(inParams); // 去空值
        String bizContent = JSONUtil.serialize(inParams); // 序列化业务参数
        outParams.put("biz_data", bizContent);
        // 将outParams转成：name=val&name2=val2...
        String signStr = DataTypeConvertUtil.map2FormData(outParams, false);
        log.info("SimpleEchoRequest:- 原签名串：{}", signStr);

        String sign = EncryptUtil.sign(signStr, thirdKey.getPriKey(), EncryptUtil.SIGN_TYPE_RSA2);
        outParams.put("sign", sign);
    }

    @Override
    public Map<String, Object> verifyParam(Map<String, Object> inParams) {
        String sign = (String) inParams.remove("sign");
        String signStr = DataTypeConvertUtil.map2FormData(inParams, false);
        log.info("SimpleEchoRequest:- 原验签串：{}", signStr);
        boolean isVerified = EncryptUtil.verify(signStr, sign, thirdKey.getParPubKey(), EncryptUtil.SIGN_TYPE_RSA2);
        if (!isVerified) {
            log.error("SimpleEchoRequest:- 验签失败，params：{}", inParams);
            return null;
        }
        return inParams;
    }

    public static void main(String[] args) {
        // RSA签名可以通过EncryptUtil.genKeyPair()生成
        EncryptUtil.genKeyPair();
    }
}
