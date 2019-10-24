package com.github.yizzuide.milkomeda.demo.echo.request;

import com.github.yizzuide.milkomeda.echo.EchoException;
import com.github.yizzuide.milkomeda.echo.EchoRequest;
import com.github.yizzuide.milkomeda.echo.EchoResponseData;
import com.github.yizzuide.milkomeda.echo.ErrorCode;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.EncryptUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SimpleEchoRequest
 * 扩展EchoRequest实现当前对接的第三方响应类型
 *
 * @author yizzuide
 * Create at 2019/09/21 20:35
 */
@Slf4j
@Component
public class SimpleEchoRequest extends EchoRequest {

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

    // 默认使用application/json，如果对接的第三方就是json通信这个方法不用覆盖
    @Override
    protected void appendHeaders(HttpHeaders headers) {
        // 如果使用application/x-www-form-urlencoded，注解下面这行
        super.appendHeaders(headers);
        // 可以设置为application/x-www-form-urlencoded
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 自定义请求头
        headers.add("Authorization", "Basic c2tfdGVzdF9PWUNFZkw1eGdMTCtsOFp2SzdRYVNHR1ZNTjczb05FcGszeXorUnhuLzJiUy9MQ2dDUVorZ3c9PTo=");
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

        String sign = EncryptUtil.sign(signStr, getPriKey(), EncryptUtil.SIGN_TYPE_RSA2);
        outParams.put("sign", sign);
    }

    @Override
    public Map<String, Object> verifyParam(Map<String, Object> inParams) {
        String sign = (String) inParams.remove("sign");
        String signStr = DataTypeConvertUtil.map2FormData(inParams, false);
        log.info("SimpleEchoRequest:- 原验签串：{}", signStr);
        boolean isVerified = EncryptUtil.verify(signStr, sign, getParPubKey(), EncryptUtil.SIGN_TYPE_RSA2);
        if (!isVerified) {
            log.error("SimpleEchoRequest:- 验签失败，params：{}", inParams);
            return null;
        }
        return inParams;
    }

    private String getParPubKey() {
        return "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCa9bXW/GTqseFLWxBfnECbxaNcMTAGDojSnmwtUcPd9mwnevRguOIDbOxbSsIwDtN9bw3o16V5N+Y7iuluEHsrWhrhC9RQx6LA9h8nuTE6c1HSstgq7y+DSPvZrbou5zZnDbgP45M2LT2MXd3HaApq+Ocvg5gp11WhKRa4AgXerQIDAQAB";
    }

    private String getPriKey() {
        return "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJr1tdb8ZOqx4UtbEF+cQJvFo1wxMAYOiNKebC1Rw932bCd69GC44gNs7FtKwjAO031vDejXpXk35juK6W4QeytaGuEL1FDHosD2Hye5MTpzUdKy2CrvL4NI+9mtui7nNmcNuA/jkzYtPYxd3cdoCmr45y+DmCnXVaEpFrgCBd6tAgMBAAECgYBuFRGZ6XFTnQw8uTN3iIwJXSzBCJxiIR8n6K1WwJhRbYbFwT4sHAtLfaym6gPrmgy6NhN+jvuZkpF3SSatLv4f3vwu3ToZcmi6A0LlVzFT7cMHBzMP/Ev09aa0N/j9+ykPlJH06ehkvwz/504GEDwLt2791MxWqtZJjuDNWloWQQJBAOaQ+jgUmjIkKX09/x0a8P13JezBP14UV5cZLvoRWW8XzTkfZx/rpC7irpijvcwBhi45kIg8JrIngYm5/QSkLDECQQCsDakrLtIJLenTSHmFi2KfI2EHYT0deFrK92+VDY15iE7gBQBvbiZiAKwjV33gcrtS6JTZWtxKeOAUUPTiUgc9AkEA1Gb+e6dPHZ3+sqfoWxG0rGuU/nRQQgUPY90JT8mn0BXnMxZg1CEqkR62pVtCv6svx2m0Yiy3oSuPxCcYlawAIQJAW5lORjJAGij6gsTkBagmkkjYoIAxdF4eIE7JdhZoCpr6OyQOjkSbZLOs8Yfj+Tm75zDyBiHshC2ERuyu40r+lQJBAJ+SImDYm9NLqo6hq1+FTI1apKyq8rsuQYL8IRsYAHmOGCNmHN1b/AFitHaptZXYOtiZyuEP8xP86Np8vrTUKSg=";
    }

    public static void main(String[] args) {
        // RSA签名可以通过EncryptUtil.genKeyPair()生成
        EncryptUtil.genKeyPair();
        // 当前生成如下：
        // priKey: MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJr1tdb8ZOqx4UtbEF+cQJvFo1wxMAYOiNKebC1Rw932bCd69GC44gNs7FtKwjAO031vDejXpXk35juK6W4QeytaGuEL1FDHosD2Hye5MTpzUdKy2CrvL4NI+9mtui7nNmcNuA/jkzYtPYxd3cdoCmr45y+DmCnXVaEpFrgCBd6tAgMBAAECgYBuFRGZ6XFTnQw8uTN3iIwJXSzBCJxiIR8n6K1WwJhRbYbFwT4sHAtLfaym6gPrmgy6NhN+jvuZkpF3SSatLv4f3vwu3ToZcmi6A0LlVzFT7cMHBzMP/Ev09aa0N/j9+ykPlJH06ehkvwz/504GEDwLt2791MxWqtZJjuDNWloWQQJBAOaQ+jgUmjIkKX09/x0a8P13JezBP14UV5cZLvoRWW8XzTkfZx/rpC7irpijvcwBhi45kIg8JrIngYm5/QSkLDECQQCsDakrLtIJLenTSHmFi2KfI2EHYT0deFrK92+VDY15iE7gBQBvbiZiAKwjV33gcrtS6JTZWtxKeOAUUPTiUgc9AkEA1Gb+e6dPHZ3+sqfoWxG0rGuU/nRQQgUPY90JT8mn0BXnMxZg1CEqkR62pVtCv6svx2m0Yiy3oSuPxCcYlawAIQJAW5lORjJAGij6gsTkBagmkkjYoIAxdF4eIE7JdhZoCpr6OyQOjkSbZLOs8Yfj+Tm75zDyBiHshC2ERuyu40r+lQJBAJ+SImDYm9NLqo6hq1+FTI1apKyq8rsuQYL8IRsYAHmOGCNmHN1b/AFitHaptZXYOtiZyuEP8xP86Np8vrTUKSg=
        // pubKey: MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCa9bXW/GTqseFLWxBfnECbxaNcMTAGDojSnmwtUcPd9mwnevRguOIDbOxbSsIwDtN9bw3o16V5N+Y7iuluEHsrWhrhC9RQx6LA9h8nuTE6c1HSstgq7y+DSPvZrbou5zZnDbgP45M2LT2MXd3HaApq+Ocvg5gp11WhKRa4AgXerQIDAQAB
    }
}
